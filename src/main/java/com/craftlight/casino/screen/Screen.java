package com.craftlight.casino.screen;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 3x3 blokluk bir Silvera ekranini temsil eder: bir duvara otomatik hizalanmis
 * 9 adet ItemFrame'den olusur ve karsisinda 3x3'luk bir "izleme alani" bulunur.
 * Oyuncu bu alanda durdugu surece ekranin kontrolu (aktif izleyici) o oyuncuya aittir.
 */
public class Screen {

    private final int id;
    private String name;
    private final Location center;           // Ekranin merkez bloğu (duvar uzerinde)
    private final BlockFace frontFace;        // Ekranin izleyiciye baktigi yon (N/S/E/W)
    private final BlockFace horizontalAxis;   // Ekran duzleminde yatay eksen (soldan saga)

    // Ekranin izleyiciye don yuzunden 2 blok uzakta, ayni yukseklikteki 3x3 zemin alani
    private final int zoneMinX, zoneMaxX, zoneMinZ, zoneMaxZ, zoneY;

    private final List<UUID> frameEntityIds = new ArrayList<>();

    public Screen(int id, String name, Location center, BlockFace frontFace, BlockFace horizontalAxis,
                  int zoneMinX, int zoneMaxX, int zoneMinZ, int zoneMaxZ, int zoneY) {
        this.id = id;
        this.name = name;
        this.center = center;
        this.frontFace = frontFace;
        this.horizontalAxis = horizontalAxis;
        this.zoneMinX = zoneMinX;
        this.zoneMaxX = zoneMaxX;
        this.zoneMinZ = zoneMinZ;
        this.zoneMaxZ = zoneMaxZ;
        this.zoneY = zoneY;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getCenter() {
        return center;
    }

    public World getWorld() {
        return center.getWorld();
    }

    public BlockFace getFrontFace() {
        return frontFace;
    }

    public BlockFace getHorizontalAxis() {
        return horizontalAxis;
    }

    public List<UUID> getFrameEntityIds() {
        return frameEntityIds;
    }

    /**
     * Verilen konum, bu ekranin karsisindaki 3x3 izleme alaninin icinde mi kontrol eder.
     * Y toleransi 1.5 blok verilir (zipla/egik zemin farklarini tolere etmek icin).
     */
    public boolean isInZone(Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().equals(getWorld())) return false;
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        double dy = Math.abs(loc.getY() - zoneY);
        return x >= zoneMinX && x <= zoneMaxX && z >= zoneMinZ && z <= zoneMaxZ && dy <= 1.5;
    }

    /**
     * Ekranin bulundugu yerin (izleme alaninin) oyuncuya gorunecek merkez konumu; teleport/gosterge icin.
     */
    public Location getZoneCenterLocation() {
        double x = (zoneMinX + zoneMaxX) / 2.0 + 0.5;
        double z = (zoneMinZ + zoneMaxZ) / 2.0 + 0.5;
        Location loc = new Location(getWorld(), x, zoneY, z);
        loc.setDirection(frontFace.getOppositeFace().getDirection());
        return loc;
    }

    public int getZoneMinX() { return zoneMinX; }
    public int getZoneMaxX() { return zoneMaxX; }
    public int getZoneMinZ() { return zoneMinZ; }
    public int getZoneMaxZ() { return zoneMaxZ; }
    public int getZoneY() { return zoneY; }

    public void removeFrames() {
        for (UUID uuid : new ArrayList<>(frameEntityIds)) {
            org.bukkit.entity.Entity e = org.bukkit.Bukkit.getEntity(uuid);
            if (e instanceof ItemFrame frame) {
                frame.remove();
            }
        }
        frameEntityIds.clear();
    }
}
