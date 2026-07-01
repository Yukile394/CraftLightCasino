package com.craftlight.casino.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack base) {
        this.item = base.clone();
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(ColorUtil.c(name));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        List<String> colored = new ArrayList<>();
        for (String s : lines) colored.add(ColorUtil.c(s));
        meta.setLore(colored);
        return this;
    }

    public ItemBuilder lore(String... lines) {
        List<String> list = new ArrayList<>();
        for (String s : lines) list.add(ColorUtil.c(s));
        meta.setLore(list);
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder flags() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    /**
     * Iteme "buyulenmis" parlama efekti (glint) verir ama buyu ismini gostermez.
     * 1.21 tarzi modern/parlak gorunum icin kullanilir (secili at, aktif bahis vb.).
     */
    public ItemBuilder glow() {
        try {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } catch (Exception ignored) {
        }
        return this;
    }

    public ItemBuilder leatherColor(Color color) {
        if (meta instanceof LeatherArmorMeta lam) {
            lam.setColor(color);
        }
        return this;
    }

    public ItemBuilder skullOwner(OfflinePlayer player) {
        if (meta instanceof SkullMeta sm) {
            sm.setOwningPlayer(player);
        }
        return this;
    }

    public ItemBuilder tag(NamespacedKey key, String value) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, value);
        return this;
    }

    public ItemBuilder tag(NamespacedKey key, int value) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.INTEGER, value);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
