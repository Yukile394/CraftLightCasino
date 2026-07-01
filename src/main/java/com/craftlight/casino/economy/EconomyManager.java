package com.craftlight.casino.economy;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Basit, dosya tabanli LCoin ekonomi sistemi.
 * Dupe/hata onlemi: her islem senkron kilit (lock) altinda yapilir ve
 * her degisiklikten hemen sonra diske yazilir. Bakiye asla negatif olamaz,
 * cekim islemlerinde bakiye yetersizse islem otomatik olarak reddedilir ya da
 * (acikca belirtilmisse) sadece mevcut miktar kadar cekilir.
 */
public class EconomyManager {

    private final CasinoPlugin plugin;
    private final File dataFile;
    private final YamlConfiguration data;
    private final ConcurrentHashMap<UUID, Double> balances = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public EconomyManager(CasinoPlugin plugin) {
        this.plugin = plugin;
        File folder = plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        this.dataFile = new File(folder, "lcoin.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("lcoin.yml olusturulamadi: " + e.getMessage());
            }
        }
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double bal = data.getDouble(key);
                balances.put(uuid, bal);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, (double) plugin.getConfig().getInt("economy.baslangic-bakiyesi", 0));
    }

    /** Bakiyeye para ekler. Miktar negatif olamaz. */
    public boolean deposit(UUID uuid, double amount) {
        if (amount <= 0) return false;
        lock.lock();
        try {
            double current = getBalance(uuid);
            double updated = round(current + amount);
            balances.put(uuid, updated);
            save(uuid, updated);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** Bakiyeden para ceker. Yetersizse false doner ve HICBIR SEY degismez (dupe/eksi bakiye onlemi). */
    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        lock.lock();
        try {
            double current = getBalance(uuid);
            if (current < amount - 0.0001) {
                return false;
            }
            double updated = round(current - amount);
            balances.put(uuid, updated);
            save(uuid, updated);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Mumkun oldugu kadar ceker (bakiye yetersizse elindeki kadarini ceker).
     * /lcoinparacek komutu icin kullanilir. Cekilen gercek miktari dondurur.
     */
    public double withdrawUpTo(UUID uuid, double amount) {
        if (amount <= 0) return 0;
        lock.lock();
        try {
            double current = getBalance(uuid);
            double toTake = Math.min(current, amount);
            if (toTake <= 0) return 0;
            double updated = round(current - toTake);
            balances.put(uuid, updated);
            save(uuid, updated);
            return toTake;
        } finally {
            lock.unlock();
        }
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount - 0.0001;
    }

    private void save(UUID uuid, double amount) {
        data.set(uuid.toString(), amount);
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("LCoin verisi kaydedilemedi: " + e.getMessage());
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public String getCurrencyName() {
        return plugin.getConfig().getString("economy.para-adi", "LCoin");
    }
}
