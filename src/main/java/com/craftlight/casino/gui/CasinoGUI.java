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

public class CasinoGUI {

    public static final int SIZE = 54;

    // Slotlar
    public static final int[] HORSE_SLOTS = {20, 21, 22, 23, 24};
    public static final int SLOT_BAHSI_BASLAT = 47;
    public static final int SLOT_BAHIS_BILGI = 49; // Simetrik - mevcut bahis burada gozukur
    public static final int SLOT_BAHIS_GERI_CEK = 51;
    public static final int SLOT_BILGI = 4;

    private final CasinoPlugin plugin;

    public CasinoGUI(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory build(Player player, CasinoArea area, CasinoSession session, CasinoColor highlighted) {
        CasinoGUIHolder holder = new CasinoGUIHolder(area.getId());
        Inventory inv = plugin.getServer().createInventory(holder, SIZE,
                ColorUtil.c("&8&l» &b&lCraft Light &7&lGazino &8&l#" + area.getId() + " &8&l«"));
        holder.setInventory(inv);

        buildModernBorder(inv, highlighted);

        // Bilgi itemi
        inv.setItem(SLOT_BILGI, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .name("&b&lNasil Oynanir?")
                .lore(
                        "&7Asagidaki &f5 attan &7birini sec,",
                        "&7secince at parlayarak isaretlenir.",
                        "",
                        "&7Ardindan &a&lBahsi Baslat &7butonuna",
                        "&7basarak yarisi baslat!",
                        "",
                        "&7Kazanirsan bahsin &a&lx2 &7katlanir!"
                ).glow().build());

        // Atlar
        for (int i = 0; i < CasinoColor.values().length; i++) {
            CasinoColor color = CasinoColor.values()[i];
            boolean selected = color == highlighted;
            inv.setItem(HORSE_SLOTS[i], buildHorseItem(color, selected, false, false));
        }

        String currency = plugin.getEconomyManager().getCurrencyName();
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        boolean hasColor = highlighted != null;
        boolean hasBet = session.getBet() > 0;

        // 1) Bahsi Baslat
        ItemBuilder startBuilder;
        if (hasColor && hasBet) {
            startBuilder = new ItemBuilder(Material.NETHERITE_INGOT)
                    .name("&a&l1) Bahsi Baslat")
                    .lore(
                            "&7Secili at: " + highlighted.getDisplayName(),
                            "&7Bahis: &6" + fmt(session.getBet()) + " " + currency,
                            "",
                            "&a&lBaslatmak icin tikla!"
                    ).glow();
        } else {
            startBuilder = new ItemBuilder(Material.IRON_INGOT)
                    .name("&7&l1) Bahsi Baslat")
                    .lore(
                            !hasColor ? "&cOnce yukaridan bir at sec!" : "&cOnce bir bahis girmelisin!",
                            "&7(/loyna <bahis> <#" + area.getId() + "> yazarak bahis girebilirsin)"
                    );
        }
        inv.setItem(SLOT_BAHSI_BASLAT, startBuilder.build());

        // 2) Simetrik bahis gostergesi - yeterli LCoin ile bahis girilmediyse hic gozukmez
        if (hasBet) {
            inv.setItem(SLOT_BAHIS_BILGI, new ItemBuilder(Material.GOLD_INGOT)
                    .name("&6&l2) Mevcut Bahsin")
                    .lore(
                            "&7Su anki bahsin:",
                            "&6&l" + fmt(session.getBet()) + " " + currency,
                            "",
                            "&7Bakiyen: &e" + fmt(balance) + " " + currency
                    ).glow().build());
        }
        // hasBet == false ise slot, asagidaki modern border deseninde kalir (gozukmez).

        // 3) Bahsi Geri Cek
        if (hasBet) {
            inv.setItem(SLOT_BAHIS_GERI_CEK, new ItemBuilder(Material.REDSTONE)
                    .name("&c&l3) Bahsi Geri Cek")
                    .lore(
                            "&7Koydugun bahsi iptal eder",
                            "&7ve parani geri verir.",
                            "",
                            "&7Mevcut Bahis: &6" + fmt(session.getBet()) + " " + currency
                    ).build());
        } else {
            inv.setItem(SLOT_BAHIS_GERI_CEK, new ItemBuilder(Material.GRAY_DYE)
                    .name("&7&l3) Bahsi Geri Cek")
                    .lore("&cGeri cekilecek bir bahsin yok.")
                    .build());
        }

        return inv;
    }

    /**
     * Tek bir at (renk) itemini olusturur. Hem normal menu gorunumunde hem de
     * CasinoGame'in yaris animasyonunda (isaretci gezinirken / racing=true) kullanilir.
     *
     * @param selected    oyuncunun bu ati sectigi/bahis oynadigi at mi
     * @param sweepActive yaris animasyonunda isaretcinin su an bu atin uzerinde olup olmadigi
     * @param racing      yaris su an devam ediyor mu (lore metnini buna gore degistirir)
     */
    public static ItemStack buildHorseItem(CasinoColor color, boolean selected, boolean sweepActive, boolean racing) {
        String prefix;
        String suffix = "";
        if (sweepActive) {
            prefix = "&f&l▶ ";
            suffix = " &f&l◀";
        } else if (selected) {
            prefix = "&f&l» ";
            suffix = " &f&l«";
        } else {
            prefix = "&7";
        }

        String loreLine1 = racing
                ? (sweepActive ? "&e&lSira burada..." : "&7Yaris suruyor, bekle...")
                : "&7Bu ata bahis oynamak icin tikla.";
        String loreLine3 = racing ? "" : (selected ? "&a&l✔ SECILI &7- Bahsi Baslat'a bas!" : "&eSecmek icin tikla");

        ItemBuilder ib = new ItemBuilder(Material.LEATHER_HORSE_ARMOR)
                .leatherColor(color.getDye())
                .name(prefix + color.getDisplayName() + suffix)
                .lore(loreLine1, "", loreLine3)
                .flags();
        if (selected || sweepActive) {
            ib.glow();
        }
        return ib.build();
    }

    /**
     * 1.21 tarzi, satranc tahtasi (checkerboard) desenli, secili renge gore
     * vurgulanan modern bir kenarlik olusturur (eski duz cam desenine kiyasla).
     */
    private void buildModernBorder(Inventory inv, CasinoColor color) {
        ItemStack accent = pane(color);
        ItemStack dark = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();

        for (int i = 0; i < SIZE; i++) {
            int row = i / 9;
            int col = i % 9;
            boolean isEdge = row == 0 || row == 5 || col == 0 || col == 8;
            if (!isEdge) continue;
            boolean checker = (row + col) % 2 == 0;
            inv.setItem(i, checker ? accent : dark);
        }
    }

    private ItemStack pane(CasinoColor color) {
        Material mat;
        if (color == null) {
            mat = Material.CYAN_STAINED_GLASS_PANE;
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
