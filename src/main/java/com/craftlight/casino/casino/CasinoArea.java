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
}
