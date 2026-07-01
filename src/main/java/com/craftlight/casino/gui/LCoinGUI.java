package com.craftlight.casino.gui;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.util.ColorUtil;
import com.craftlight.casino.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LCoinGUI {

    public static final int SIZE = 27;
    public static final int SLOT_HEAD = 13;

    private final CasinoPlugin plugin;

    public LCoinGUI(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory build(Player player) {
        LCoinGUIHolder holder = new LCoinGUIHolder();
        Inventory inv = plugin.getServer().createInventory(holder, SIZE, ColorUtil.c("&8Craft Light &7» &6LCoin Bakiyem"));
        holder.setInventory(inv);

        ItemStack border = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, border);
        }

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        String currency = plugin.getEconomyManager().getCurrencyName();
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name("&6&l" + player.getName())
                .lore(
                        "&7━━━━━━━━━━━━━━━━━━",
                        "&fBakiyen: &a&l" + fmt(balance) + " " + currency,
                        "&7━━━━━━━━━━━━━━━━━━",
                        "",
                        "&7" + currency + ", Craft Light sunucusunun",
                        "&7kendi ekonomi para birimidir.",
                        "&7Gazinoda kazanip market'ten",
                        "&7esya satin alabilirsin!",
                        "",
                        "&8Guncellendi: " + date
                ).build();
        inv.setItem(SLOT_HEAD, head);

        return inv;
    }

    private String fmt(double val) {
        if (val == Math.floor(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }
}
