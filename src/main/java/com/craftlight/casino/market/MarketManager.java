package com.craftlight.casino.market;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketManager {

    private final CasinoPlugin plugin;
    private final File file;
    private final YamlConfiguration cfg;
    private final Map<Integer, MarketItem> items = new ConcurrentHashMap<>();
    public final int maxItem;

    public MarketManager(CasinoPlugin plugin) {
        this.plugin = plugin;
        this.maxItem = plugin.getConfig().getInt("market.max-item", 15);
        File folder = plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        this.file = new File(folder, "market.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("market.yml olusturulamadi: " + e.getMessage());
            }
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        for (String key : cfg.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                String name = cfg.getString(key + ".name", "Esya");
                double price = cfg.getDouble(key + ".price", 0);
                int slot = cfg.getInt(key + ".slot", -1);
                String itemData = cfg.getString(key + ".item");
                ItemStack stack = deserialize(itemData);
                if (stack == null) continue;
                MarketItem mi = new MarketItem(id, name, stack, price);
                mi.setSlot(slot);
                items.put(id, mi);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public void save() {
        for (Map.Entry<Integer, MarketItem> e : items.entrySet()) {
            MarketItem mi = e.getValue();
            String key = String.valueOf(e.getKey());
            cfg.set(key + ".name", mi.getName());
            cfg.set(key + ".price", mi.getPrice());
            cfg.set(key + ".slot", mi.getSlot());
            cfg.set(key + ".item", serialize(mi.getItem()));
        }
        // Silinen id'leri temizle
        for (String key : new java.util.HashSet<>(cfg.getKeys(false))) {
            try {
                int id = Integer.parseInt(key);
                if (!items.containsKey(id)) {
                    cfg.set(key, null);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("market.yml kaydedilemedi: " + e.getMessage());
        }
    }

    private String serialize(ItemStack item) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(out);
            boos.writeObject(item);
            boos.close();
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().severe("Item serialize edilemedi: " + e.getMessage());
            return null;
        }
    }

    private ItemStack deserialize(String data) {
        if (data == null) return null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream bois = new BukkitObjectInputStream(in);
            ItemStack item = (ItemStack) bois.readObject();
            bois.close();
            return item;
        } catch (Exception e) {
            plugin.getLogger().severe("Item deserialize edilemedi: " + e.getMessage());
            return null;
        }
    }

    public boolean addItem(String name, int id, ItemStack item, double defaultPrice) {
        if (items.containsKey(id)) return false;
        if (items.size() >= maxItem) return false;
        MarketItem mi = new MarketItem(id, name, item.clone(), defaultPrice);
        items.put(id, mi);
        save();
        return true;
    }

    public boolean removeItem(int id) {
        boolean removed = items.remove(id) != null;
        if (removed) save();
        return removed;
    }

    public MarketItem get(int id) {
        return items.get(id);
    }

    public MarketItem getBySlot(int slot) {
        for (MarketItem mi : items.values()) {
            if (mi.getSlot() == slot) return mi;
        }
        return null;
    }

    public Map<Integer, MarketItem> getItems() {
        return new LinkedHashMap<>(items);
    }

    public boolean setPrice(int id, double price) {
        MarketItem mi = items.get(id);
        if (mi == null) return false;
        mi.setPrice(price);
        save();
        return true;
    }

    public void setSlot(int id, int slot) {
        MarketItem mi = items.get(id);
        if (mi == null) return;
        mi.setSlot(slot);
        save();
    }
}
