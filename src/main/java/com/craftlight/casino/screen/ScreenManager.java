package com.craftlight.casino.screen;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Silvera Ekran Yansitma Sistemi.
 * Oyuncunun baktigi yuzeye otomatik hizalanmis, 3x3 ItemFrame + Harita tabanli
 * "ekranlar" olusturur, yonetir ve karsisindaki 3x3 izleme alanina giren/cikan
 * oyunculari performansli sekilde takip eder (lag olusturmaz, sadece blok
 * degisiminde hafif bir kontrol yapilir).
 */
public class ScreenManager {

    private final CasinoPlugin plugin;
    private final File file;
    private final YamlConfiguration cfg;

    private final Map<Integer, Screen> screens = new ConcurrentHashMap<>();
    // Her ekran icin o an izleme alaninda olan (kontrolu elinde tutan) oyuncu
    private final Map<Integer, UUID> activeViewers = new ConcurrentHashMap<>();
    // Bir oyuncunun en son hangi blok konumunda kontrol edildigini tutar (gereksiz kontrolu onlemek icin)
    private final Map<UUID, Long> lastCheckedBlock = new ConcurrentHashMap<>();

    public ScreenManager(CasinoPlugin plugin) {
        this.plugin = plugin;
        File folder = plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        this.file = new File(folder, "screens.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("screens.yml olusturulamadi: " + e.getMessage());
            }
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Oyuncunun baktigi yuzeye tam ortalanmis, otomatik hizalanmis yeni bir 3x3 ekran olusturur.
     * Donus: hata mesaji varsa String, basariliysa null.
     */
    public String createAtLookedSurface(Player player, int id, String name) {
        if (screens.containsKey(id)) {
            return "Bu ID (#" + id + ") zaten kullaniliyor. Once /ayarla ile kaldirin ya da farkli bir ID secin.";
        }

        Block target = getTargetWall(player);
        if (target == null) {
            return "Bir duvara (katı bloğa) bakmalısın! En fazla 12 blok mesafede olmalı.";
        }

        BlockFace lookDir = cardinal(player.getLocation().getYaw());
        BlockFace frontFace = lookDir.getOppositeFace(); // Ekranin oyuncuya baktigi yon
        BlockFace horizontalAxis = (lookDir == BlockFace.NORTH || lookDir == BlockFace.SOUTH)
                ? BlockFace.EAST : BlockFace.SOUTH;

        World world = target.getWorld();
        Location center = target.getLocation().add(0.5, 0.5, 0.5);

        // Izleme alani: ekrandan 2 blok uzakta, zemin oyuncunun ayak hizasinda (merkezin 1 alti)
        Block zoneCenterBlock = target.getRelative(frontFace, 2);
        int zoneY = zoneCenterBlock.getY() - 1;
        Vector axisVec = horizontalAxis.getDirection();

        int ax = zoneCenterBlock.getX() + (int) axisVec.getX();
        int az = zoneCenterBlock.getZ() + (int) axisVec.getZ();
        int bx = zoneCenterBlock.getX() - (int) axisVec.getX();
        int bz = zoneCenterBlock.getZ() - (int) axisVec.getZ();

        int minX = Math.min(ax, Math.min(bx, zoneCenterBlock.getX()));
        int maxX = Math.max(ax, Math.max(bx, zoneCenterBlock.getX()));
        int minZ = Math.min(az, Math.min(bz, zoneCenterBlock.getZ()));
        int maxZ = Math.max(az, Math.max(bz, zoneCenterBlock.getZ()));

        Screen screen = new Screen(id, name, center, frontFace, horizontalAxis, minX, maxX, minZ, maxZ, zoneY);
        spawnFrames(screen, target);
        screens.put(id, screen);
        save();
        return null;
    }

    private void spawnFrames(Screen screen, Block wallCenter) {
        BlockFace frontFace = screen.getFrontFace();
        BlockFace axis = screen.getHorizontalAxis();
        Vector axisVec = axis.getDirection();
        World world = wallCenter.getWorld();

        // row: -1 = ust, 0 = orta, 1 = alt | col: -1 = sol, 0 = orta, 1 = sag
        for (int row = -1; row <= 1; row++) {
            for (int col = -1; col <= 1; col++) {
                int wx = wallCenter.getX() + (int) axisVec.getX() * col;
                int wz = wallCenter.getZ() + (int) axisVec.getZ() * col;
                int wy = wallCenter.getY() - row; // row=-1 -> yukari (y+1)
                Block wallBlock = world.getBlockAt(wx, wy, wz);
                Block airBlock = wallBlock.getRelative(frontFace);

                ItemFrame frame = world.spawn(airBlock.getLocation(), ItemFrame.class, f -> {
                    f.setFacingDirection(frontFace, true);
                    f.setFixed(true);
                    f.setInvulnerable(true);
                    f.setPersistent(true);
                    f.setVisible(true);
                    f.setRotation(org.bukkit.Rotation.NONE);
                });

                MapView view = Bukkit.createMap(world);
                view.getRenderers().forEach(r -> view.removeRenderer(r));
                view.addRenderer(new ScreenMapRenderer(col + 1, row + 1));
                view.setLocked(true);

                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                org.bukkit.inventory.meta.MapMeta meta = (org.bukkit.inventory.meta.MapMeta) mapItem.getItemMeta();
                meta.setMapView(view);
                mapItem.setItemMeta(meta);

                frame.setItem(mapItem, false);
                screen.getFrameEntityIds().add(frame.getUniqueId());
            }
        }
    }

    /**
     * Oyuncunun baktigi ilk kati (hava olmayan) blogu bulur.
     */
    private Block getTargetWall(Player player) {
        var result = player.rayTraceBlocks(12);
        if (result == null || result.getHitBlock() == null) return null;
        Block block = result.getHitBlock();
        if (block.getType().isAir() || !block.getType().isSolid()) return null;
        return block;
    }

    private BlockFace cardinal(float yaw) {
        float y = yaw % 360;
        if (y < 0) y += 360;
        if (y >= 45 && y < 135) return BlockFace.WEST;
        if (y >= 135 && y < 225) return BlockFace.NORTH;
        if (y >= 225 && y < 315) return BlockFace.EAST;
        return BlockFace.SOUTH;
    }

    public boolean remove(int id) {
        Screen screen = screens.remove(id);
        if (screen == null) return false;
        screen.removeFrames();
        activeViewers.remove(id);
        cfg.set(String.valueOf(id), null);
        save();
        return true;
    }

    public Screen get(int id) {
        return screens.get(id);
    }

    public Map<Integer, Screen> getScreens() {
        return screens;
    }

    /**
     * Oyuncu hareket ettiginde cagrilir (blok degisiminde). Tum ekranlarin izleme
     * alanlarini kontrol eder; girdi/cikti tespit edilirse kontrolu gunceller.
     * Performans: sadece oyuncunun blok konumu degistiginde tetiklenmeli (listener'da filtrelenir).
     */
    public void checkZones(Player player, Location newLoc) {
        UUID uuid = player.getUniqueId();
        for (Screen screen : screens.values()) {
            boolean inZone = screen.isInZone(newLoc);
            UUID currentViewer = activeViewers.get(screen.getId());
            boolean wasViewer = uuid.equals(currentViewer);

            if (inZone && !wasViewer) {
                if (currentViewer == null) {
                    activeViewers.put(screen.getId(), uuid);
                    player.sendMessage(com.craftlight.casino.util.ColorUtil.c(
                            "&#4DA3FF&l[Ekran] &fArtik &#4DA3FF" + screen.getName() + " &fekraninin kontrolu sende!"));
                }
            } else if (!inZone && wasViewer) {
                activeViewers.remove(screen.getId());
                player.sendMessage(com.craftlight.casino.util.ColorUtil.c("&cEkran alanindan ciktin, kontrol serbest birakildi."));
            }
        }
    }

    public UUID getActiveViewer(int screenId) {
        return activeViewers.get(screenId);
    }

    /**
     * Oyuncu sunucudan ayrildiginda kontrol ettigi ekranlari serbest birakir.
     */
    public void releasePlayer(UUID uuid) {
        activeViewers.values().removeIf(v -> v.equals(uuid));
        lastCheckedBlock.remove(uuid);
    }

    public Map<UUID, Long> getLastCheckedBlock() {
        return lastCheckedBlock;
    }

    private void save() {
        for (Map.Entry<Integer, Screen> entry : screens.entrySet()) {
            Screen s = entry.getValue();
            String key = String.valueOf(entry.getKey());
            cfg.set(key + ".name", s.getName());
            cfg.set(key + ".world", s.getWorld().getName());
            cfg.set(key + ".x", s.getCenter().getX());
            cfg.set(key + ".y", s.getCenter().getY());
            cfg.set(key + ".z", s.getCenter().getZ());
            cfg.set(key + ".front", s.getFrontFace().name());
            cfg.set(key + ".axis", s.getHorizontalAxis().name());
            cfg.set(key + ".zoneMinX", s.getZoneMinX());
            cfg.set(key + ".zoneMaxX", s.getZoneMaxX());
            cfg.set(key + ".zoneMinZ", s.getZoneMinZ());
            cfg.set(key + ".zoneMaxZ", s.getZoneMaxZ());
            cfg.set(key + ".zoneY", s.getZoneY());
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("screens.yml kaydedilemedi: " + e.getMessage());
        }
    }

    /**
     * Sunucu acilisinda kayitli ekranlarin cercevelerini (item frame) yeniden olusturur.
     * Dunya yuklu degilse o ekran atlanir ve tekrar denenmez (elle /loynaekranyansit ile yeniden yapilmali).
     */
    public void loadAll() {
        for (String key : cfg.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                World world = Bukkit.getWorld(cfg.getString(key + ".world", ""));
                if (world == null) continue;
                String name = cfg.getString(key + ".name", "Ekran #" + id);
                double x = cfg.getDouble(key + ".x");
                double y = cfg.getDouble(key + ".y");
                double z = cfg.getDouble(key + ".z");
                BlockFace front = BlockFace.valueOf(cfg.getString(key + ".front", "SOUTH"));
                BlockFace axis = BlockFace.valueOf(cfg.getString(key + ".axis", "EAST"));
                int minX = cfg.getInt(key + ".zoneMinX");
                int maxX = cfg.getInt(key + ".zoneMaxX");
                int minZ = cfg.getInt(key + ".zoneMinZ");
                int maxZ = cfg.getInt(key + ".zoneMaxZ");
                int zoneY = cfg.getInt(key + ".zoneY");

                Location center = new Location(world, x, y, z);
                Screen screen = new Screen(id, name, center, front, axis, minX, maxX, minZ, maxZ, zoneY);
                Block wallCenter = center.clone().subtract(0.5, 0.5, 0.5).getBlock();
                spawnFrames(screen, wallCenter);
                screens.put(id, screen);
            } catch (Exception e) {
                plugin.getLogger().warning("Ekran #" + key + " yuklenemedi: " + e.getMessage());
            }
        }
    }

    public void removeAllFrames() {
        for (Screen screen : screens.values()) {
            screen.removeFrames();
        }
    }
}
