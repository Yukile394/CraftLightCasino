package com.craftlight.casino.gui;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.screen.Screen;
import com.craftlight.casino.util.ColorUtil;
import com.craftlight.casino.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ScreenAyarlaGUI {

    public static final int SIZE = 54;
    public final NamespacedKey screenIdKey;

    private final CasinoPlugin plugin;

    public ScreenAyarlaGUI(CasinoPlugin plugin) {
        this.plugin = plugin;
        this.screenIdKey = new NamespacedKey(plugin, "screen_id");
    }

    public Inventory build(Player viewer) {
        ScreenAyarlaGUIHolder holder = new ScreenAyarlaGUIHolder();
        Inventory inv = plugin.getServer().createInventory(holder, SIZE,
                ColorUtil.c("&8✦ &#4DA3FF&lSilvera &#C0C0C0» &fEkran Yonetimi &8✦"));
        holder.setInventory(inv);

        border(inv);

        List<Screen> screens = plugin.getScreenManager().getScreens().values().stream()
                .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                .toList();

        if (screens.isEmpty()) {
            inv.setItem(22, new ItemBuilder(Material.BARRIER)
                    .name("&c&lHenuz ekran yok")
                    .lore("&7/loynaekranyansit <ID> <Isim>",
                          "&7komutuyla yeni bir Silvera ekrani",
                          "&7olusturabilirsin.")
                    .build());
            return inv;
        }

        int slot = 10;
        for (Screen screen : screens) {
            if (slot % 9 == 8) slot += 2; // kenarlara denk gelmesin
            if (slot >= 44) break;

            boolean active = plugin.getScreenManager().getActiveViewer(screen.getId()) != null;
            ItemStack item = new ItemBuilder(Material.ITEM_FRAME)
                    .name("&#4DA3FF&l#" + screen.getId() + " &f" + screen.getName())
                    .lore(
                            "&#C0C0C0Konum: &f" + fmt(screen.getCenter()),
                            "&#C0C0C0Yon: &f" + screen.getFrontFace().name(),
                            "&#C0C0C0Durum: " + (active ? "&a&lIzleniyor" : "&7Bos"),
                            "",
                            "&e▶ Sol tik: ekranin yanina isin",
                            "&c▶ Sag tik: ekrani sil"
                    ).tag(screenIdKey, screen.getId()).build();

            inv.setItem(slot, item);
            slot++;
        }

        return inv;
    }

    private String fmt(org.bukkit.Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    private void border(Inventory inv) {
        ItemStack pane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        ItemStack accent = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < SIZE; i++) {
            int row = i / 9;
            int col = i % 9;
            boolean isEdge = row == 0 || row == 5 || col == 0 || col == 8;
            if (!isEdge) continue;
            boolean checker = (row + col) % 2 == 0;
            inv.setItem(i, checker ? accent : pane);
        }
    }
}
