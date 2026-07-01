package com.craftlight.casino.casino;

import org.bukkit.Location;

import java.util.UUID;

public class CasinoArea {

    private final int id;
    private Location pos1;
    private Location pos2;
    private Location blockLoc;
    private boolean running = false;
    private UUID activePlayer = null;

    public CasinoArea(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public Location getBlockLoc() {
        return blockLoc;
    }

    public void setBlockLoc(Location blockLoc) {
        this.blockLoc = blockLoc;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public UUID getActivePlayer() {
        return activePlayer;
    }

    public void setActivePlayer(UUID activePlayer) {
        this.activePlayer = activePlayer;
    }

    public boolean isFullyConfigured() {
        return pos1 != null && pos2 != null && blockLoc != null;
    }

    public boolean isOccupied() {
        return running || activePlayer != null;
    }

    /**
     * Verilen konumun bu gazino alaninin (pos1 - pos2 arasindaki kutunun)
     * icinde olup olmadigini kontrol eder. /loyna komutunu kullanmak icin
     * oyuncunun gazinonun uzerinde/icinde durmasi gerekir.
     */
    public boolean isPlayerInside(Location loc) {
        if (pos1 == null || pos2 == null || loc == null) return false;
        if (pos1.getWorld() == null || loc.getWorld() == null) return false;
        if (!pos1.getWorld().equals(loc.getWorld())) return false;

        int minX = (int) Math.floor(Math.min(pos1.getX(), pos2.getX()));
        int maxX = (int) Math.floor(Math.max(pos1.getX(), pos2.getX()));
        int minY = (int) Math.floor(Math.min(pos1.getY(), pos2.getY())) - 1;
        int maxY = (int) Math.floor(Math.max(pos1.getY(), pos2.getY())) + 1;
        int minZ = (int) Math.floor(Math.min(pos1.getZ(), pos2.getZ()));
        int maxZ = (int) Math.floor(Math.max(pos1.getZ(), pos2.getZ()));

        int px = (int) Math.floor(loc.getX());
        int py = (int) Math.floor(loc.getY());
        int pz = (int) Math.floor(loc.getZ());

        return px >= minX && px <= maxX && py >= minY && py <= maxY && pz >= minZ && pz <= maxZ;
    }
}
