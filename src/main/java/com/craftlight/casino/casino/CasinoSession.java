package com.craftlight.casino.casino;

import java.util.UUID;

public class CasinoSession {

    private final UUID player;
    private final int areaId;
    private double bet = 0;
    private boolean racing = false;
    private CasinoColor pendingColor = null;

    public CasinoSession(UUID player, int areaId) {
        this.player = player;
        this.areaId = areaId;
    }

    public UUID getPlayer() {
        return player;
    }

    public int getAreaId() {
        return areaId;
    }

    public double getBet() {
        return bet;
    }

    public void setBet(double bet) {
        this.bet = bet;
    }

    public boolean isRacing() {
        return racing;
    }

    public void setRacing(boolean racing) {
        this.racing = racing;
    }

    public CasinoColor getPendingColor() {
        return pendingColor;
    }

    public void setPendingColor(CasinoColor pendingColor) {
        this.pendingColor = pendingColor;
    }
}
