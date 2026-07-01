package com.craftlight.casino.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class MarketGUIHolder implements InventoryHolder {

    public enum Mode { VIEW, EDIT_POSITION }

    private final Mode mode;
    private Inventory inventory;

    public MarketGUIHolder(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
