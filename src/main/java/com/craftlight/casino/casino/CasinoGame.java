package com.craftlight.casino.casino;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * Secilen renk block'unun (goksusagi beton) hizli hizli renk degistirip
 * sonunda rastgele bir renkte durmasini saglayan yaris animasyonu.
 * Oyuncu dogru rengi tahmin ettiyse bahsi katlanir.
 */
public class CasinoGame {

    private final CasinoPlugin plugin;
    private final Random random = new Random();

    public CasinoGame(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    public void startRace(Player player, CasinoArea area, CasinoSession session, CasinoColor picked, Runnable onFinish) {
        if (area.getBlockLoc() == null) {
            player.sendMessage("§cBu gazinonun blok konumu ayarlanmamis! Yetkiliye bildirin.");
            return;
        }
        area.setRunning(true);
        session.setRacing(true);
        Location blockLoc = area.getBlockLoc();
        double bet = session.getBet();

        new BukkitRunnable() {
            int ticks = 0;
            final int totalTicks = plugin.getConfig().getInt("casino.yaris-suresi-saniye", 5) * 20;

            @Override
            public void run() {
                if (ticks >= totalTicks) {
                    // Sonucu belirle
                    CasinoColor result = CasinoColor.random();
                    blockLoc.getBlock().setType(result.getConcrete());
                    boolean won = result == picked;
                    double multiplier = plugin.getConfig().getDouble("casino.kazanma-carpani", 2.0);

                    if (won) {
                        double winnings = round(bet * multiplier);
                        plugin.getEconomyManager().deposit(player.getUniqueId(), winnings);
                        player.sendTitle("§a§lKAZANDIN!", "§e+" + winnings + " " + plugin.getEconomyManager().getCurrencyName(), 10, 60, 10);
                        player.sendMessage("§a§l[Craft Light Gazino] §aTebrikler! " + result.getDisplayName() + " §akazandi ve §e" + winnings + " " + plugin.getEconomyManager().getCurrencyName() + " §akazandin!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    } else {
                        player.sendTitle("§c§lKAYBETTIN", "§7Kazanan: " + result.getDisplayName(), 10, 60, 10);
                        player.sendMessage("§c§l[Craft Light Gazino] §cKaybettin! Kazanan renk: " + result.getDisplayName());
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }

                    area.setRunning(false);
                    area.setActivePlayer(null);
                    session.setRacing(false);
                    session.setBet(0);
                    onFinish.run();
                    cancel();
                    return;
                }

                // Hizlanan / yavaslayan spin efekti
                int interval = ticks < totalTicks - 30 ? 3 : Math.max(1, (totalTicks - ticks) / 6);
                if (ticks % Math.max(1, interval) == 0) {
                    CasinoColor spin = CasinoColor.random();
                    blockLoc.getBlock().setType(spin.getConcrete());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.4f);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
