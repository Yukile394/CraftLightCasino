package com.craftlight.casino.gui;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.market.MarketItem;
import com.craftlight.casino.util.ColorUtil;
import com.craftlight.casino.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;

public class MarketGUI {

    public static final int SIZE = 54;

    // 15 esya icin simetrik 3 satir x 5 sutun yerlesim
    public static final int[] ITEM_SLOTS = {
            20, 21, 22, 23, 24,
            29, 30, 31, 32, 33,
            38, 39, 40, 41, 42
    };

    public static final int SLOT_HEAD = 4;

    private final CasinoPlugin plugin;

    public MarketGUI(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory build(Player viewer, MarketGUIHolder.Mode mode) {
        MarketGUIHolder holder = new MarketGUIHolder(mode);
        String title = mode == MarketGUIHolder.Mode.EDIT_POSITION
                ? "&8Craft Light &7» &6Market &8(&eYer Ayarlama&8)"
                : "&8Craft Light &7» &6Market";
        Inventory inv = plugin.getServer().createInventory(holder, SIZE, ColorUtil.c(title));
        holder.setInventory(inv);

        ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, border);
        }
        for (int slot : ITEM_SLOTS) {
            inv.setItem(slot, new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        // Kafa
        ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(viewer)
                .name("&6&l" + viewer.getName())
                .lore(
                        "&7Bakiyen: &e" + fmt(plugin.getEconomyManager().getBalance(viewer.getUniqueId())) + " " + plugin.getEconomyManager().getCurrencyName(),
                        "",
                        mode == MarketGUIHolder.Mode.EDIT_POSITION
                                ? "&eBir esyaya tikla, sonra tasimak"
                                        + " istedigin bos/dolu slota tikla."
                                : "&7Satin almak istedigin esyaya tikla!"
                ).build();
        inv.setItem(SLOT_HEAD, head);

        Map<Integer, MarketItem> items = plugin.getMarketManager().getItems();
        // Slotu atanmamis itemleri sirayla bos slotlara yerlestir
        boolean[] used = new boolean[ITEM_SLOTS.length];
        for (MarketItem mi : items.values()) {
            int idx = slotIndex(mi.getSlot());
            if (idx != -1) used[idx] = true;
        }
        int fillPointer = 0;
        for (MarketItem mi : items.values()) {
            int idx = slotIndex(mi.getSlot());
            if (idx == -1) {
                while (fillPointer < used.length && used[fillPointer]) fillPointer++;
                if (fillPointer >= used.length) continue;
                idx = fillPointer;
                used[fillPointer] = true;
                mi.setSlot(ITEM_SLOTS[idx]);
            }
            inv.setItem(ITEM_SLOTS[idx], buildDisplayItem(mi, mode));
        }

        return inv;
    }

    private ItemStack buildDisplayItem(MarketItem mi, MarketGUIHolder.Mode mode) {
        ItemStack display = mi.getItem().clone();
        ItemBuilder ib = new ItemBuilder(display);
        ib.name("&b&l" + mi.getName());
        if (mode == MarketGUIHolder.Mode.EDIT_POSITION) {
            ib.lore(
                    "&7ID: &f#" + mi.getId(),
                    "&7Fiyat: &6" + fmt(mi.getPrice()) + " " + plugin.getEconomyManager().getCurrencyName(),
                    "",
                    "&eYerini degistirmek icin tikla"
            );
        } else {
            ib.lore(
                    "&7ID: &f#" + mi.getId(),
                    "",
                    "&6" + fmt(mi.getPrice()) + " " + plugin.getEconomyManager().getCurrencyName(),
                    "",
                    "&a&lSatin almak icin tikla!"
            );
        }
        return ib.build();
    }

    private int slotIndex(int slot) {
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            if (ITEM_SLOTS[i] == slot) return i;
        }
        return -1;
    }

    private String fmt(double val) {
        if (val == Math.floor(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }
}
