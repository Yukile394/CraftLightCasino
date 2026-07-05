package com.craftlight.casino.protection;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Yeni oyuncu korumasinin (koruma aktifken PvP/item alma/portal engeli) verilerini
 * tutar ve koruma.yml dosyasinda kalici olarak saklar. Boylece bir oyuncu koruma
 * bitmeden sunucudan cikip tekrar girerse kaldigi yerden (kalan blok sayisiyla) devam eder.
 */
public class ProtectionManager {

    private final CasinoPlugin plugin;
    private final File file;
    private final YamlConfiguration cfg;

    private final int baslangicBlok;

    // aktif korumasi olan oyuncular
    private final Set<UUID> active = ConcurrentHashMap.newKeySet();
    // oyuncu -> kalan blok sayisi
    private final java.util.Map<UUID, Integer> remaining = new ConcurrentHashMap<>();

    public ProtectionManager(CasinoPlugin plugin) {
        this.plugin = plugin;
        this.baslangicBlok = plugin.getConfig().getInt("koruma.baslangic-blok", 200);

        File folder = plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        this.file = new File(folder, "koruma.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("koruma.yml olusturulamadi: " + e.getMessage());
            }
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        for (String key : cfg.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                boolean aktif = cfg.getBoolean(key + ".aktif", false);
                int kalan = cfg.getInt(key + ".kalan", baslangicBlok);
                if (aktif) {
                    active.add(uuid);
                    remaining.put(uuid, kalan);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        for (UUID uuid : allKnownUuids()) {
            String key = uuid.toString();
            cfg.set(key + ".aktif", active.contains(uuid));
            cfg.set(key + ".kalan", remaining.getOrDefault(uuid, baslangicBlok));
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("koruma.yml kaydedilemedi: " + e.getMessage());
        }
    }

    private Set<UUID> allKnownUuids() {
        Set<UUID> all = ConcurrentHashMap.newKeySet();
        all.addAll(active);
        all.addAll(remaining.keySet());
        for (String key : cfg.getKeys(false)) {
            try {
                all.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return all;
    }

    /** Oyuncunun daha once hic koruma kaydi olup olmadigini (ilk giris mi) soyler. */
    public boolean hasRecord(UUID uuid) {
        return cfg.contains(uuid.toString());
    }

    public boolean isActive(UUID uuid) {
        return active.contains(uuid);
    }

    public int getRemaining(UUID uuid) {
        return remaining.getOrDefault(uuid, baslangicBlok);
    }

    public int getBaslangicBlok() {
        return baslangicBlok;
    }

    /** Yeni oyuncu icin korumayi ilk kez baslatir. */
    public void activateForNewPlayer(UUID uuid) {
        active.add(uuid);
        remaining.put(uuid, baslangicBlok);
        save();
    }

    /**
     * Aktif korumali bir oyuncu blok kirdiginda cagrilir. Kalan sayiyi bir azaltir
     * ve 0'a ulasilip ulasilmadigini dondurur (0'a ulastiysa koruma burada kapatilir).
     */
    public boolean decrementOnBlockBreak(UUID uuid) {
        if (!active.contains(uuid)) return false;
        int kalan = remaining.getOrDefault(uuid, baslangicBlok) - 1;
        if (kalan <= 0) {
            remaining.put(uuid, 0);
            active.remove(uuid);
            save();
            return true;
        }
        remaining.put(uuid, kalan);
        save();
        return false;
    }
}

