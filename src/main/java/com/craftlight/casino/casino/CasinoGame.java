package com.craftlight.casino.casino;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.gui.CasinoGUI;
import com.craftlight.casino.gui.CasinoGUIHolder;
import com.craftlight.casino.util.ItemBuilder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * Oyuncu bir at secip bahsi baslattiktan sonra, gazino menusu KAPANMADAN icinde
 * 4 saniye suren bir yaris animasyonu oynatilir: isaretci 5 atin arasinda bir
 * ileri bir geri (tek gidis - tek gelis) gezinir ve sonunda rastgele bir atin
 * uzerinde durur. Durdugu at oyuncunun sectigi atsa YESIL beton ile
 * "Tebrikler Kazandin!" yazar ve bahis x2 katlanir; degilse KIRMIZI beton ile
 * "Kaybettin!" yazar.
 */
public class CasinoGame {

    private final CasinoPlugin plugin;
    private final Random random = new Random();

    // Isaretcinin gezindigi yol: 0 -> 4 ileri, 4 -> 0 geri (tek gidis - tek gelis)
    private static final int[] SWEEP_PATH = {0, 1, 2, 3, 4, 3, 2, 1, 0};

    public CasinoGame(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    public void startRace(Player player, CasinoArea area, CasinoSession session, CasinoColor picked, Runnable onFinish) {
        area.setRunning(true);
        session.setRacing(true);
        double bet = session.getBet();
        String currency = plugin.getEconomyManager().getCurrencyName();

        // Sonuc bastan belirlenir: 5 attan biri kazanir (%20 sans, kazaninca x2).
        CasinoColor result = CasinoColor.random();
        boolean won = result == picked;
        double multiplier = plugin.getConfig().getDouble("casino.kazanma-carpani", 2.0);
        double winnings = round(bet * multiplier);
        int resultIndex = indexOf(result);

        Location blockLoc = area.getBlockLoc();

        int totalTicks = plugin.getConfig().getInt("casino.yaris-suresi-saniye", 4) * 20;
        int sweepTicks = Math.max(20, totalTicks - 10); // son yarim saniye sonucu "acikliyor"
        int stepInterval = Math.max(2, sweepTicks / SWEEP_PATH.length);

        new BukkitRunnable() {
            int ticks = 0;
            int lastSecond = -1;

            @Override
            public void run() {
                Inventory topInv = getOpenCasinoInventory(player, area);

                if (ticks >= totalTicks) {
                    if (topInv != null) {
                        revealResult(topInv, picked, result, won, bet, winnings, currency);
                    }
                    if (blockLoc != null) {
                        blockLoc.getBlock().setType(result.getConcrete());
                    }

                    if (won) {
                        plugin.getEconomyManager().deposit(player.getUniqueId(), winnings);
                        player.sendTitle("§a§lTEBRIKLER KAZANDIN!", "§e+" + fmt(winnings) + " " + currency, 10, 60, 10);
                        player.sendMessage("§a§l[Craft Light Gazino] §aTebrikler! " + result.getDisplayName()
                                + " §akazandi ve §e" + fmt(winnings) + " " + currency + " §akazandin!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    } else {
                        player.sendTitle("§c§lKAYBETTIN!", "§7Kazanan: " + result.getDisplayName(), 10, 60, 10);
                        player.sendMessage("§c§l[Craft Light Gazino] §cKaybettin! Kazanan at: " + result.getDisplayName()
                                + " §7- Bahsin (" + fmt(bet) + " " + currency + ") gitti.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }

                    area.setRunning(false);
                    area.setActivePlayer(null);
                    session.setRacing(false);
                    session.setBet(0);
                    session.setPendingColor(null);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onFinish.run();
                            // Oyuncu hala gazino menusundeyse menuyu sifirlayip tekrar acar.
                            Inventory refreshed = getOpenCasinoInventory(player, area);
                            if (refreshed != null) {
                                plugin.openInventorySilently(player, plugin.getCasinoGUI().build(player, area, session, null));
                            }
                        }
                    }.runTaskLater(plugin, 40L);
                    cancel();
                    return;
                }

                if (ticks < sweepTicks) {
                    int remainingSeconds = (int) Math.ceil((sweepTicks - ticks) / 20.0);
                    if (remainingSeconds != lastSecond) {
                        lastSecond = remainingSeconds;
                        sendActionBar(player, "§6§l⏳ Kalan sure: §e" + remainingSeconds);
                    }
                } else if (ticks == sweepTicks) {
                    sendActionBar(player, "§e§lCekilis yapiliyor...");
                }

                if (topInv != null) {
                    int idx = ticks < sweepTicks
                            ? SWEEP_PATH[(ticks / stepInterval) % SWEEP_PATH.length]
                            : resultIndex;
                    animateSweep(topInv, idx, picked);
                    if (ticks < sweepTicks && ticks % stepInterval == 0) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.3f);
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private Inventory getOpenCasinoInventory(Player player, CasinoArea area) {
        InventoryView view = player.getOpenInventory();
        if (view == null) return null;
        Inventory top = view.getTopInventory();
        if (top.getHolder() instanceof CasinoGUIHolder holder && holder.getAreaId() == area.getId()) {
            return top;
        }
        return null;
    }

    private void animateSweep(Inventory inv, int sweepIndex, CasinoColor picked) {
        CasinoColor[] values = CasinoColor.values();
        for (int i = 0; i < values.length; i++) {
            CasinoColor color = values[i];
            boolean selected = color == picked;
            boolean sweepActive = i == sweepIndex;
            inv.setItem(CasinoGUI.HORSE_SLOTS[i], CasinoGUI.buildHorseItem(color, selected, sweepActive, true));
        }
    }

    private void revealResult(Inventory inv, CasinoColor picked, CasinoColor result, boolean won, double bet, double winnings, String currency) {
        CasinoColor[] values = CasinoColor.values();
        for (int i = 0; i < values.length; i++) {
            CasinoColor color = values[i];
            if (color == result) {
                ItemBuilder ib = won
                        ? new ItemBuilder(Material.GREEN_CONCRETE)
                        .name("&a&lTebrikler Kazandin!")
                        .lore(
                                "&7Kazanan at: " + result.getDisplayName(),
                                "&7Bahsin: &6" + fmt(bet) + " " + currency,
                                "&a&lKazandigin: &e+" + fmt(winnings) + " " + currency,
                                "",
                                "&a✔ Bahis basariyla tamamlandi!"
                        ).glow()
                        : new ItemBuilder(Material.RED_CONCRETE)
                        .name("&c&lKaybettin!")
                        .lore(
                                "&7Kazanan at: " + result.getDisplayName(),
                                "&7Kaybettigin bahis: &c-" + fmt(bet) + " " + currency,
                                "&7Bir dahaki sefere sansin yaver gider!",
                                "",
                                "&c✘ Bahis kaybedildi."
                        );
                inv.setItem(CasinoGUI.HORSE_SLOTS[i], ib.build());
            } else {
                boolean selected = color == picked;
                inv.setItem(CasinoGUI.HORSE_SLOTS[i], CasinoGUI.buildHorseItem(color, selected, false, false));
            }
        }
    }

    private void sendActionBar(Player player, String legacyText) {
        try {
            player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(legacyText));
        } catch (Exception ignored) {
        }
    }

    private int indexOf(CasinoColor color) {
        CasinoColor[] values = CasinoColor.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == color) return i;
        }
        return 0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String fmt(double val) {
        if (val == Math.floor(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }
}
