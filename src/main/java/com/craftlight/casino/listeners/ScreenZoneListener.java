package com.craftlight.casino.listeners;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Ekranlarin karsisindaki 3x3 izleme alanlarina giris/cikisi tespit eder.
 * Lag olusturmamak icin sadece oyuncunun BULUNDUGU BLOK degistiginde
 * (her tik degil) kontrol calisir; ekran sayisi az oldugundan bu kontrol ucuzdur.
 */
public class ScreenZoneListener implements Listener {

    private final CasinoPlugin plugin;

    public ScreenZoneListener(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (plugin.getScreenManager().getScreens().isEmpty()) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Sadece blok degisiminde kontrol et - performans icin
        }
        plugin.getScreenManager().checkZones(event.getPlayer(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (plugin.getScreenManager().getScreens().isEmpty()) return;
        plugin.getScreenManager().checkZones(event.getPlayer(), event.getTo());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Oyuncu ayrildiginda kontrol ettigi ekranlari serbest birak
        plugin.getScreenManager().releasePlayer(event.getPlayer().getUniqueId());
    }
}

