package com.craftlight.casino.gui;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.casino.CasinoArea;
import com.craftlight.casino.casino.CasinoColor;
import com.craftlight.casino.casino.CasinoSession;
import com.craftlight.casino.util.ColorUtil;
import com.craftlight.casino.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class CasinoGUI {

    public static final int SIZE = 54;

    // Slotlar
    public static final int[] HORSE_SLOTS = {20, 21, 22, 23, 24};
    public static final int SLOT_BAHIS_EKLE = 47;
    public static final int SLOT_BAHIS_AYARLA = 49;
    public static final int SLOT_BAHIS_GERI_CEK = 51;
    public static final int SLOT_BILGI = 13;

    private final CasinoPlugin plugin;

    public CasinoGUI(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory build(Player player, CasinoArea area, CasinoSession session, CasinoColor highlighted) {
        CasinoGUIHolder holder = new CasinoGUIHolder(area.getId());
        Inventory inv = plugin.getServer().createInventory(holder, SIZE, ColorUtil.c("&8Craft Light &7» &6Gazino &8#" + area.getId()));
        holder.setInventory(inv);

        ItemStack border = glass(highlighted);
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, border);
        }

        // Bilgi itemi
        inv.setItem(SLOT_BILGI, new ItemBuilder(Material.PAPER)
                .name("&6&lNasil Oynanir?")
                .lore(
                        "&7Asagidaki 5 attan birini sec.",
                        "&7Once bahis miktarini belirle,",
                        "&7ardindan bir ata tiklayarak",
                        "&7yarisi baslat!",
                        "",
                        "&7Kazanirsan bahsin &ax2 &7katlanir!"
                ).build());

        // Atlar
        for (int i = 0; i < CasinoColor.values().length; i++) {
            CasinoColor color = CasinoColor.values()[i];
            boolean selected = color == highlighted;
            ItemBuilder ib = new ItemBuilder(Material.LEATHER_HORSE_ARMOR)
                    .leatherColor(color.getDye())
                    .name((selected ? "&l» " : "") + color.getDisplayName() + (selected ? " &l«" : ""))
                    .lore(
                            "&7Bu ata bahis oynamak icin tikla.",
                            "",
                            selected ? "&a&lSECILI - Yarisi baslatmak icin tekrar tikla!" : "&eSecmek icin tikla"
                    ).flags();
            inv.setItem(HORSE_SLOTS[i], ib.build());
        }

        String currency = plugin.getEconomyManager().getCurrencyName();
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        int betIncrement = plugin.getConfig().getInt("casino.bahis-artis-miktari", 100);

        inv.setItem(SLOT_BAHIS_EKLE, new ItemBuilder(Material.EMERALD)
                .name("&a&l1) Bahis Ekle (+" + betIncrement + ")")
                .lore(
                        "&7Bakiyenden &e" + betIncrement + " " + currency + " &7ekleyerek",
                        "&7bahsini artirir.",
                        "",
                        "&7Bakiyen: &6" + fmt(balance) + " " + currency
                ).build());

        inv.setItem(SLOT_BAHIS_AYARLA, new ItemBuilder(Material.ANVIL)
                .name("&e&l2) Bahsi Kendin Ayarla")
                .lore(
                        "&7Tikla ve sohbete istedigin",
                        "&7bahis miktarini yaz.",
                        "",
                        "&7Mevcut Bahis: &6" + fmt(session.getBet()) + " " + currency
                ).build());

        inv.setItem(SLOT_BAHIS_GERI_CEK, new ItemBuilder(Material.REDSTONE)
                .name("&c&l3) Bahsi Geri Cek")
                .lore(
                        "&7Koydugun bahsi iptal eder",
                        "&7ve parani geri verir.",
                        "",
                        "&7Mevcut Bahis: &6" + fmt(session.getBet()) + " " + currency
                ).build());

        return inv;
    }

    private ItemStack glass(CasinoColor color) {
        Material mat;
        if (color == null) {
            mat = Material.GRAY_STAINED_GLASS_PANE;
        } else {
            mat = switch (color) {
                case KIRMIZI -> Material.RED_STAINED_GLASS_PANE;
                case SARI -> Material.YELLOW_STAINED_GLASS_PANE;
                case YESIL -> Material.LIME_STAINED_GLASS_PANE;
                case MAVI -> Material.BLUE_STAINED_GLASS_PANE;
                case MOR -> Material.PURPLE_STAINED_GLASS_PANE;
            };
        }
        return new ItemBuilder(mat).name(" ").build();
    }

    private String fmt(double val) {
        if (val == Math.floor(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }
}
