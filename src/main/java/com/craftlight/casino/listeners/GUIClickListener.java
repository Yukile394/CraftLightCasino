package com.craftlight.casino.listeners;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.casino.CasinoArea;
import com.craftlight.casino.casino.CasinoColor;
import com.craftlight.casino.casino.CasinoSession;
import com.craftlight.casino.gui.CasinoGUI;
import com.craftlight.casino.gui.CasinoGUIHolder;
import com.craftlight.casino.gui.LCoinGUIHolder;
import com.craftlight.casino.gui.MarketGUI;
import com.craftlight.casino.gui.MarketGUIHolder;
import com.craftlight.casino.market.MarketItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIClickListener implements Listener {

    private final CasinoPlugin plugin;
    // Yer ayarlama modunda "elde tutulan" market esyasi id'si
    private final Map<UUID, Integer> editSelection = new ConcurrentHashMap<>();

    public GUIClickListener(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof CasinoGUIHolder || holder instanceof MarketGUIHolder || holder instanceof LCoinGUIHolder) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (!(holder instanceof CasinoGUIHolder) && !(holder instanceof MarketGUIHolder) && !(holder instanceof LCoinGUIHolder)) {
            return;
        }
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) return; // alt envanter tiklamalarini yok say

        if (holder instanceof CasinoGUIHolder casinoHolder) {
            handleCasinoClick(player, casinoHolder, slot);
        } else if (holder instanceof MarketGUIHolder marketHolder) {
            handleMarketClick(player, marketHolder, e.getInventory(), slot);
        }
        // LCoinGUIHolder icin tiklamada bir islem yapilmaz (sadece bilgi menusu)
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (!(holder instanceof CasinoGUIHolder casinoHolder)) return;
        if (!(e.getPlayer() instanceof Player player)) return;

        CasinoSession session = plugin.getSession(player.getUniqueId());
        if (session == null) return;
        if (session.isRacing()) return; // yaris devam ederken kapatilirsa dokunma

        CasinoArea area = plugin.getCasinoManager().get(casinoHolder.getAreaId());
        if (session.getBet() > 0) {
            plugin.getEconomyManager().deposit(player.getUniqueId(), session.getBet());
            player.sendMessage("§e§l[Gazino] §eMenuyu kapattigin icin bahsin (" + fmt(session.getBet()) + " " + plugin.getEconomyManager().getCurrencyName() + ") iade edildi.");
            session.setBet(0);
        }
        session.setPendingColor(null);
        if (area != null && !area.isRunning()) {
            area.setActivePlayer(null);
        }
    }

    private void handleCasinoClick(Player player, CasinoGUIHolder casinoHolder, int slot) {
        int areaId = casinoHolder.getAreaId();
        CasinoArea area = plugin.getCasinoManager().get(areaId);
        CasinoSession session = plugin.getSession(player.getUniqueId());
        if (area == null || session == null) {
            player.closeInventory();
            return;
        }
        if (session.isRacing()) return; // yaris surerken tiklamalar yok sayilir

        // At secimi - artik sadece secim/isaretleme yapar, yarisi baslatmaz
        for (int i = 0; i < CasinoGUI.HORSE_SLOTS.length; i++) {
            if (CasinoGUI.HORSE_SLOTS[i] == slot) {
                CasinoColor color = CasinoColor.values()[i];
                if (session.getPendingColor() == color) {
                    // Ayni ata tekrar tiklarsa secimi kaldirir
                    session.setPendingColor(null);
                    player.openInventory(plugin.getCasinoGUI().build(player, area, session, null));
                } else {
                    session.setPendingColor(color);
                    player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1.4f);
                    player.openInventory(plugin.getCasinoGUI().build(player, area, session, color));
                }
                return;
            }
        }

        if (slot == CasinoGUI.SLOT_BAHSI_BASLAT) {
            CasinoColor color = session.getPendingColor();
            if (color == null) {
                player.sendMessage("§cOnce yukaridaki 5 attan birini secmelisin!");
                return;
            }
            if (session.getBet() <= 0) {
                player.sendMessage("§cOnce bir bahis girmelisin! §7(/loyna <bahis> <#" + areaId + ">)");
                return;
            }
            player.closeInventory();
            plugin.getCasinoGame().startRace(player, area, session, color, () -> {
                session.setPendingColor(null);
            });
        } else if (slot == CasinoGUI.SLOT_BAHIS_GERI_CEK) {
            if (session.getBet() <= 0) {
                player.sendMessage("§cGeri cekilecek bir bahsin yok.");
                return;
            }
            plugin.getEconomyManager().deposit(player.getUniqueId(), session.getBet());
            player.sendMessage("§a§l[Gazino] §aBahsin (" + fmt(session.getBet()) + " " + plugin.getEconomyManager().getCurrencyName() + ") iade edildi.");
            session.setBet(0);
            session.setPendingColor(null);
            player.openInventory(plugin.getCasinoGUI().build(player, area, session, null));
        }
    }

    private void handleMarketClick(Player player, MarketGUIHolder marketHolder, Inventory inv, int slot) {
        if (slot == MarketGUI.SLOT_HEAD) return;

        boolean isItemSlot = false;
        for (int s : MarketGUI.ITEM_SLOTS) {
            if (s == slot) {
                isItemSlot = true;
                break;
            }
        }
        if (!isItemSlot) return;

        MarketItem clicked = plugin.getMarketManager().getBySlot(slot);

        if (marketHolder.getMode() == MarketGUIHolder.Mode.EDIT_POSITION) {
            Integer selected = editSelection.get(player.getUniqueId());
            if (selected == null) {
                if (clicked != null) {
                    editSelection.put(player.getUniqueId(), clicked.getId());
                    player.sendMessage("§e#" + clicked.getId() + " (" + clicked.getName() + ") secildi. Simdi tasimak istedigin slota tikla.");
                }
                return;
            } else {
                MarketItem source = plugin.getMarketManager().get(selected);
                editSelection.remove(player.getUniqueId());
                if (source == null) return;
                int sourceSlot = source.getSlot();
                if (clicked != null) {
                    // Slotlari takas et
                    plugin.getMarketManager().setSlot(clicked.getId(), sourceSlot);
                    plugin.getMarketManager().setSlot(source.getId(), slot);
                } else {
                    plugin.getMarketManager().setSlot(source.getId(), slot);
                }
                player.sendMessage("§a" + source.getName() + " esyasinin yeri guncellendi!");
                player.openInventory(plugin.getMarketGUI().build(player, MarketGUIHolder.Mode.EDIT_POSITION));
                return;
            }
        }

        // VIEW mode - satin alma
        if (clicked == null) return;
        UUID uuid = player.getUniqueId();
        double price = clicked.getPrice();
        if (price <= 0) {
            player.sendMessage("§cBu esyanin fiyati henuz ayarlanmamis!");
            return;
        }
        if (!plugin.getEconomyManager().has(uuid, price)) {
            player.sendMessage("§c§l[Market] §cYetersiz bakiye! Bu esya §6" + fmt(price) + " " + plugin.getEconomyManager().getCurrencyName() + " §ctutuyor.");
            return;
        }

        ItemStack giveItem = clicked.getItem().clone();
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§cEnvanterinde yer yok! Once yer ac.");
            return;
        }

        // Dupe onlemi: once para cekilir, basarisiz olursa esya verilmez.
        if (!plugin.getEconomyManager().withdraw(uuid, price)) {
            player.sendMessage("§cIslem sirasinda bir hata olustu, tekrar dene.");
            return;
        }
        player.getInventory().addItem(giveItem);
        player.sendMessage("§a✔ §a§lBasariyla Itemi Satin Aldin! §7(-" + fmt(price) + " " + plugin.getEconomyManager().getCurrencyName() + ")");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.3f);
        player.openInventory(plugin.getMarketGUI().build(player, MarketGUIHolder.Mode.VIEW));
    }

    private String fmt(double val) {
        if (val == Math.floor(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }
}
