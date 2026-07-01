package com.craftlight.casino.gui;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.market.MarketItem;
import com.craftlight.casino.util.ColorUtil;
import com.craftlight.casino.util.GradientUtil;
import com.craftlight.casino.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketGUI {

    public static final int SIZE = 54;

    // 15 esya icin simetrik 3 satir x 5 sutun yerlesim
    public static final int[] ITEM_SLOTS = {
            20, 21, 22, 23, 24,
            29, 30, 31, 32, 33,
            38, 39, 40, 41, 42
    };

    public static final int SLOT_HEAD = 4;

    private final CasinoPlugin plugin;
    private final NamespacedKey marketIdKey;

    public MarketGUI(CasinoPlugin plugin) {
        this.plugin = plugin;
        this.marketIdKey = new NamespacedKey(plugin, "clc_market_item_id");
    }

    public Inventory build(Player viewer, MarketGUIHolder.Mode mode) {
        MarketGUIHolder holder = new MarketGUIHolder(mode);
        String title = mode == MarketGUIHolder.Mode.EDIT_POSITION
                ? "&8✦ &6Craft Light &7» &fMarket &8(&eYer Ayarlama&8) &8✦"
                : "&8✦ &6Craft Light &7» &fMarket &8✦";
        Inventory inv = plugin.getServer().createInventory(holder, SIZE, ColorUtil.c(title));
        holder.setInventory(inv);

        ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, border);
        }
        for (int slot : ITEM_SLOTS) {
            inv.setItem(slot, new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        // Kafa
        ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(viewer)
                .name(headName(viewer.getName(), 0.0))
                .lore(
                        "&7Bakiyen: &e" + fmt(plugin.getEconomyManager().getBalance(viewer.getUniqueId())) + " " + plugin.getEconomyManager().getCurrencyName(),
                        "",
                        mode == MarketGUIHolder.Mode.EDIT_POSITION
                                ? "&eBir esyaya tikla, sonra tasimak"
                                        + " istedigin bos/dolu slota tikla."
                                : "&7Satin almak istedigin esyaya tikla!"
                ).build();
        inv.setItem(SLOT_HEAD, head);

        Map<Integer, MarketItem> items = plugin.getMarketManager().getItems();
        // Slotu atanmamis itemleri sirayla bos slotlara yerlestir
        boolean[] used = new boolean[ITEM_SLOTS.length];
        for (MarketItem mi : items.values()) {
            int idx = slotIndex(mi.getSlot());
            if (idx != -1) used[idx] = true;
        }
        int fillPointer = 0;
        for (MarketItem mi : items.values()) {
            int idx = slotIndex(mi.getSlot());
            if (idx == -1) {
                while (fillPointer < used.length && used[fillPointer]) fillPointer++;
                if (fillPointer >= used.length) continue;
                idx = fillPointer;
                used[fillPointer] = true;
                mi.setSlot(ITEM_SLOTS[idx]);
            }
            inv.setItem(ITEM_SLOTS[idx], buildDisplayItem(mi, mode));
        }

        return inv;
    }

    private ItemStack buildDisplayItem(MarketItem mi, MarketGUIHolder.Mode mode) {
        // Orijinal lore, eklenen esyanin ustune yazilmadan once yakalanir (varsa korunur)
        List<String> originalLore = capturedLore(mi);

        ItemStack display = mi.getItem().clone();
        ItemBuilder ib = new ItemBuilder(display);
        ib.name(itemName(mi.getName(), 0.0));
        ib.tag(marketIdKey, mi.getId());
        ib.lore(buildLore(mi, mode, 0.0, originalLore));
        return ib.build();
    }

    /**
     * Esyanin /lmarketesyaekle ile eklenirken elde zaten sahip oldugu lore'u yakalar.
     * Bu lore, daha sonra market goruntusune eklenirken uzerine yazilmaz, korunur.
     */
    private List<String> capturedLore(MarketItem mi) {
        ItemStack raw = mi.getItem();
        if (raw == null || raw.getItemMeta() == null) return null;
        List<String> lore = raw.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return null;
        return lore;
    }

    /**
     * Esyanin zaten bir lore'u varsa oldugu gibi korunur, altina kucuk ve sik
     * "Craft Light > Tikla ve Satin Al" markalama satirlari (pembe-beyaz akan RGB) eklenir.
     * Esyanin lore'u yoksa dogrudan sadece bu markalama satirlari gosterilir.
     * ID/fiyat gibi sabit bilgiler her zaman en altta kalir.
     * phase, animasyon icin renklerin kaydirilma miktaridir (0.0 = statik ilk hal).
     */
    private List<String> buildLore(MarketItem mi, MarketGUIHolder.Mode mode, double phase, List<String> originalLore) {
        List<String> lore = new ArrayList<>();

        if (originalLore != null && !originalLore.isEmpty()) {
            lore.addAll(originalLore);
            lore.add("");
        }

        lore.add(GradientUtil.flow("✦ Craft Light", phase, GradientUtil.PINK_WHITE));
        lore.add(GradientUtil.flow("➤ Tikla ve Satin Al", phase + 0.12, GradientUtil.PINK_WHITE));

        lore.add("");
        lore.add(ColorUtil.c("&7ID: &f#" + mi.getId()));
        if (mode == MarketGUIHolder.Mode.EDIT_POSITION) {
            lore.add(ColorUtil.c("&7Fiyat: &6" + fmt(mi.getPrice()) + " " + plugin.getEconomyManager().getCurrencyName()));
            lore.add("");
            lore.add(ColorUtil.c("&eYerini degistirmek icin tikla"));
        } else {
            lore.add(ColorUtil.c("&6" + fmt(mi.getPrice()) + " " + plugin.getEconomyManager().getCurrencyName()));
            lore.add("");
            lore.add(ColorUtil.c("&a&lSatin almak icin tikla!"));
        }
        return lore;
    }

    /** Esya ismi icin kucuk zarif isaretlerle sarilmis pembe-beyaz akan RGB isim. */
    private String itemName(String name, double phase) {
        return GradientUtil.flow("✦ " + name + " ✦", phase, GradientUtil.PINK_WHITE);
    }

    /** Ust kisimdaki oyuncu kafasi icin daha sik/premium altin-beyaz akan RGB isim. */
    private String headName(String playerName, double phase) {
        return GradientUtil.flow("★ " + playerName, phase, GradientUtil.GOLD_WHITE);
    }

    /**
     * Su an acik olan Market GUI'lerindeki item isimlerini/lorelerini ve oyuncu
     * kafasinin ismini, verilen animasyon fazina gore yeniden boyar (akan RGB efekti).
     * Sadece meta (isim/lore) guncellenir; slot/materyal degismez.
     */
    public void refreshAnimation(Player viewer, Inventory inv, MarketGUIHolder.Mode mode, double phase) {
        ItemStack head = inv.getItem(SLOT_HEAD);
        if (head != null && head.getType() == Material.PLAYER_HEAD) {
            ItemMeta hm = head.getItemMeta();
            if (hm != null) {
                hm.setDisplayName(headName(viewer.getName(), phase));
                head.setItemMeta(hm);
            }
        }

        for (int slot : ITEM_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item == null) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            Integer id = meta.getPersistentDataContainer().get(marketIdKey, PersistentDataType.INTEGER);
            if (id == null) continue;
            MarketItem mi = plugin.getMarketManager().get(id);
            if (mi == null) continue;

            meta.setDisplayName(itemName(mi.getName(), phase));
            meta.setLore(buildLore(mi, mode, phase, capturedLore(mi)));
            item.setItemMeta(meta);
        }
    }

    private int slotIndex(int slot) {
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            if (ITEM_SLOTS[i] == slot) return i;
        }
        return -1;
    }

    private String fmt(double val) {
        if (val == Math.floor(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }
}
