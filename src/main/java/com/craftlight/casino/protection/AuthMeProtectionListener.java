package com.craftlight.casino.protection;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.util.ColorUtil;
import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.RegisterEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Korumayi AuthMe ile uyumlu hale getirir.
 *
 * Oyuncu sunucuya baglandiginda (PlayerJoinEvent) koruma ARTIK baslamiyor.
 * Koruma sadece AuthMe'nin basarili dogrulama eventlerinde baslar:
 *  - Ilk giris: /register <sifre> <sifre> basariyla tamamlaninca (RegisterEvent)
 *  - Sonraki girisler: /login <sifre> basariyla tamamlaninca (LoginEvent)
 *
 * Bu eventler AuthMe tarafindan SADECE dogrulama basarili olduktan SONRA
 * tetiklenir, bu yuzden koruma ve suresi dogrulama tamamlanmadan asla
 * baslamaz / azalmaz.
 */
public class AuthMeProtectionListener implements Listener {

    private static final String PREFIX = "&8[&9Craft Light&8] &7» ";

    private final CasinoPlugin plugin;

    public AuthMeProtectionListener(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    /** Ilk kayit basariyla tamamlandiginda koruma ilk kez baslar. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthMeRegister(RegisterEvent e) {
        Player player = e.getPlayer();
        ProtectionManager pm = plugin.getProtectionManager();
        UUID uuid = player.getUniqueId();

        if (pm.hasRecord(uuid)) return; // zaten kaydi var, tekrar baslatma

        pm.activateForNewPlayer(uuid);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            sendKorumaTitle(player, "&b&lKORUMA AKTİF", subtitle(pm.getRemaining(uuid)));
            player.sendMessage(ColorUtil.c(PREFIX + "&fKayıt başarılı! &b" + pm.getBaslangicBlok()
                    + " blok&7'luk yeni oyuncu koruman şimdi aktif edildi. &7(&b/koruma bilgi&7)"));
        });
    }

    /** Basarili giris tamamlandiginda (kayitliysa) koruma baslar / kaldigi yerden devam eder. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthMeLogin(LoginEvent e) {
        Player player = e.getPlayer();
        ProtectionManager pm = plugin.getProtectionManager();
        UUID uuid = player.getUniqueId();

        if (!pm.hasRecord(uuid)) {
            // Normalde register once tetiklenir, ancak bir sebeple hic kaydi
            // yoksa (ornegin premium auto-login) burada ilk kez aktif ediyoruz.
            pm.activateForNewPlayer(uuid);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                sendKorumaTitle(player, "&b&lKORUMA AKTİF", subtitle(pm.getRemaining(uuid)));
                player.sendMessage(ColorUtil.c(PREFIX + "&fGiriş başarılı! &b" + pm.getBaslangicBlok()
                        + " blok&7'luk yeni oyuncu koruman şimdi aktif edildi. &7(&b/koruma bilgi&7)"));
            });
            return;
        }

        if (pm.isActive(uuid)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                sendKorumaTitle(player, "&b&lKORUMAN HÂLÂ AKTİF", subtitle(pm.getRemaining(uuid)));
                player.sendMessage(ColorUtil.c(PREFIX + "&fGiriş başarılı! Koruman hala aktif, kaldığın yerden devam ediyor. &7(&b/koruma bilgi&7)"));
            });
        }
    }

    private void sendKorumaTitle(Player player, String mainTitle, String subtitle) {
        player.sendTitle(ColorUtil.c(mainTitle), subtitle, 0, 30, 5);
    }

    private String subtitle(int kalan) {
        return ColorUtil.c("&7• &fKorumanın Bitmesine &b&l" + kalan + " &7Blok Kaldı &7•");
    }
}

