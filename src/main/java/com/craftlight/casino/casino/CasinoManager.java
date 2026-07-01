package com.craftlight.casino.casino;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CasinoManager {

    private final CasinoPlugin plugin;
    private final File file;
    private final YamlConfiguration cfg;
    private final Map<Integer, CasinoArea> areas = new ConcurrentHashMap<>();
    public final int maxAlan;

    public CasinoManager(CasinoPlugin plugin) {
        this.plugin = plugin;
        this.maxAlan = plugin.getConfig().getInt("casino.max-alan", 20);
        File folder = plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        this.file = new File(folder, "casinos.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("casinos.yml olusturulamadi: " + e.getMessage());
            }
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        for (String key : cfg.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                CasinoArea area = new CasinoArea(id);
                area.setPos1(loc(cfg.getString(key + ".pos1")));
                area.setPos2(loc(cfg.getString(key + ".pos2")));
                area.setBlockLoc(loc(cfg.getString(key + ".blok")));
                areas.put(id, area);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public void save() {
        for (Map.Entry<Integer, CasinoArea> entry : areas.entrySet()) {
            CasinoArea a = entry.getValue();
            String key = String.valueOf(entry.getKey());
            cfg.set(key + ".pos1", str(a.getPos1()));
            cfg.set(key + ".pos2", str(a.getPos2()));
            cfg.set(key + ".blok", str(a.getBlockLoc()));
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("casinos.yml kaydedilemedi: " + e.getMessage());
        }
    }

    private String str(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ();
    }

    private Location loc(String s) {
        if (s == null) return null;
        String[] parts = s.split(";");
        if (parts.length < 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        return new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    public CasinoArea getOrCreate(int id) {
        return areas.computeIfAbsent(id, CasinoArea::new);
    }

    public CasinoArea get(int id) {
        return areas.get(id);
    }

    public boolean isValidId(int id) {
        return id >= 1 && id <= maxAlan;
    }

    public Map<Integer, CasinoArea> getAreas() {
        return areas;
    }
}
