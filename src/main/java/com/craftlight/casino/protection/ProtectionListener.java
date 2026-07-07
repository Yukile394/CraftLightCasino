package com.craftlight.casino.protection;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.util.ColorUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProtectionListener implements Listener {

    private static final String PREFIX = "&8[&9Craft Light&8] &7» ";

    private final CasinoPlugin plugin;

    // item alma uyarisi icin spam onleme (uuid -> son mesaj zamani ms)
    private final Map<UUID, Long> pickupWarnCooldown = new ConcurrentHashMap<>();
    // portal uyarisi icin spam onleme
    private final Map<UUID, Long> portalWarnCooldown = new ConcurrentHashMap<>();

    public ProtectionListener(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    // NOT: Koruma artik PlayerJoinEvent'te DEGIL, AuthMe'nin basarili
    // RegisterEvent / LoginEvent'lerinde (bkz. AuthMeProtectionListener) baslatiliyor.
    // Boylece oyuncu sunucuya baglandigi anda degil, kayit/giris tamamlandiginda
    // koruma aktif olup suresi islemeye baslar.

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        pickupWarnCooldown.remove(e.getPlayer().getUniqueId());
        portalWarnCooldown.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        ProtectionManager pm = plugin.getProtectionManager();
        UUID uuid = player.getUniqueId();
        if (!pm.isActive(uuid)) return;

        boolean bitti = pm.decrementOnBlockBreak(uuid);
        if (bitti) {
            player.sendTitle(
                    ColorUtil.c("&a&lKORUMA SONA ERDİ!"),
                    ColorUtil.c("&7Artık &c&lPvP Aktif&7! Dikkatli ol."),
                    5, 60, 10
            );
            player.sendMessage(ColorUtil.c(PREFIX + "&fYeni oyuncu koruman &c&lsona erdi&7! Artık diğer oyunculara vurabilir, onlardan vurulabilirsin."));
        } else {
            sendKorumaActionBar(player, pm.getRemaining(uuid));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;

        Player attacker = resolveAttacker(e.getDamager());
        if (attacker == null) return;

        ProtectionManager pm = plugin.getProtectionManager();
        boolean attackerKorumali = pm.isActive(attacker.getUniqueId());
        boolean victimKorumali = pm.isActive(victim.getUniqueId());

        if (!attackerKorumali && !victimKorumali) return;

        // Kesin iptal: hasari sifirla, olayi iptal et ve tepkiyi (knockback) da geri al
        // ki tek bir vurus, iptal edilmeden once sizan bir tepkiyle "2 vurus" gibi hissettirmesin.
        e.setDamage(0);
        e.setCancelled(true);

        Vector victimVel = victim.getVelocity().clone();
        Vector attackerVel = attacker.getVelocity().clone();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (victim.isOnline()) victim.setVelocity(victimVel);
            if (attacker.isOnline()) attacker.setVelocity(attackerVel);
        });

        if (attackerKorumali) {
            attacker.sendMessage(ColorUtil.c(PREFIX + "&fBaşlangıç koruman Varken Başkalarına Saldıramazsın!"));
        } else {
            attacker.sendMessage(ColorUtil.c(PREFIX + "&f Bu oyuncu başlangıç koruması altında. Ona saldıramazsın!"));
        }
    }

    private Player resolveAttacker(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        ProtectionManager pm = plugin.getProtectionManager();
        if (!pm.isActive(player.getUniqueId())) return;

        e.setCancelled(true);

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = pickupWarnCooldown.getOrDefault(uuid, 0L);
        if (now - last < 1500L) return;
        pickupWarnCooldown.put(uuid, now);

        player.sendMessage(ColorUtil.c(PREFIX + "&fKoruma Aktifken Yerden Eşya Alamazsın!"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        ProtectionManager pm = plugin.getProtectionManager();
        if (!pm.isActive(player.getUniqueId())) return;

        PlayerTeleportEvent.TeleportCause cause = e.getCause();
        boolean portal = cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
                || cause == PlayerTeleportEvent.TeleportCause.END_PORTAL
                || cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY
                || cause == PlayerTeleportEvent.TeleportCause.PLUGIN;
        if (!portal) return;

        e.setCancelled(true);

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = portalWarnCooldown.getOrDefault(uuid, 0L);
        if (now - last < 2000L) return;
        portalWarnCooldown.put(uuid, now);

        player.sendMessage(ColorUtil.c(PREFIX + "&fKoruma Aktifken Portallardan Geçemezsin! &7(&b/koruma bilgi&7)"));
    }

    private void sendKorumaActionBar(Player player, int kalan) {
        String mesaj = ColorUtil.c("&b&lKorumanın bitmesine kalan blok: &f&l" + kalan);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(mesaj));
    }
}
