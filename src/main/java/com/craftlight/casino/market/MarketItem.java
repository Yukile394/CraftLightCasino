package com.craftlight.casino.market;

import org.bukkit.inventory.ItemStack;

public class MarketItem {

    private final int id;
    private String name;
    private ItemStack item;
    private double price;
    private int slot = -1;

    public MarketItem(int id, String name, ItemStack item, double price) {
        this.id = id;
        this.name = name;
        this.item = item;
        this.price = price;
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

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }
}
