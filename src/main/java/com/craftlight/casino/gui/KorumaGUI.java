package com.craftlight.casino.gui;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.util.ColorUtil;
import com.craftlight.casino.util.GradientUtil;
import com.craftlight.casino.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * /koruma bilgi ile acilan, yeni oyuncu korumasi hakkinda tam bilgi veren menu.
 * Ortada bir oyuncu kafasi bulunur; ismi ve lore'u mavi-beyaz akan RGB ile boyanir.
 */
public class KorumaGUI {

    public static final int SIZE = 27;
    public static final int SLOT_HEAD = 13;

    private final CasinoPlugin plugin;

    public KorumaGUI(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory build(Player viewer) {
        KorumaGUIHolder holder = new KorumaGUIHolder();
        Inventory inv = plugin.getServer().createInventory(holder, SIZE, ColorUtil.c("&8✦ &b&lCraft Light &7» &fKoruma Bilgi &8✦"));
        holder.setInventory(inv);

        ItemStack border = new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, border);
        }

        inv.setItem(SLOT_HEAD, buildHead(viewer, 0.0));
        return inv;
    }

    private ItemStack buildHead(Player viewer, double phase) {
        boolean aktif = plugin.getProtectionManager().isActive(viewer.getUniqueId());
        int kalan = plugin.getProtectionManager().getRemaining(viewer.getUniqueId());
        int baslangic = plugin.getProtectionManager().getBaslangicBlok();

        return new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(viewer)
                .name(headName(phase))
                .lore(buildLore(aktif, kalan, baslangic, phase))
                .build();
    }

    private String headName(double phase) {
        return GradientUtil.flow("★ Koruma Bilgi ★", phase, GradientUtil.BLUE_WHITE);
    }

    private List<String> buildLore(boolean aktif, int kalan, int baslangic, double phase) {
        List<String> lore = new ArrayList<>();
        lore.add(GradientUtil.flow("━━━━━━━━━━━━━━━━━━━━", phase, GradientUtil.BLUE_WHITE));
        lore.add(ColorUtil.c("&fDurum: " + (aktif ? "&b&lAKTİF" : "&7&lKAPALI")));
        if (aktif) {
            lore.add(ColorUtil.c("&fKalan Blok: &b&l" + kalan + " &7/ " + baslangic));
        }
        lore.add("");
        lore.add(GradientUtil.flow("✦ Yeni Oyuncu Koruması Nedir?", phase + 0.1, GradientUtil.BLUE_WHITE));
        lore.add(ColorUtil.c("&7Sunucuya ilk katıldığında otomatik"));
        lore.add(ColorUtil.c("&7olarak &b" + baslangic + " blok&7'luk bir koruma"));
        lore.add(ColorUtil.c("&7süreci başlatılır."));
        lore.add("");
        lore.add(ColorUtil.c("&f• &7Koruma aktifken &cbaşka oyunculara"));
        lore.add(ColorUtil.c("&7  &cvuramazsın&7, onlar da sana vuramaz."));
        lore.add(ColorUtil.c("&f• &7Yerden &cesya alamazsın&7."));
        lore.add(ColorUtil.c("&f• &7Portallardan &cgeçemezsin&7."));
        lore.add("");
        lore.add(ColorUtil.c("&f• &7Her kırdığın blokta koruma süresi"));
        lore.add(ColorUtil.c("&7  &b1 blok&7 azalır."));
        lore.add(ColorUtil.c("&f• &b" + baslangic + " &7blok kırınca koruma biter"));
        lore.add(ColorUtil.c("&7  ve &aPvP aktif olur&7."));
        lore.add("");
        lore.add(ColorUtil.c("&f• &7Koruma bitmeden çıkarsan, girişinde"));
        lore.add(ColorUtil.c("&7  kaldığın yerden devam eder."));
        lore.add(GradientUtil.flow("━━━━━━━━━━━━━━━━━━━━", phase + 0.2, GradientUtil.BLUE_WHITE));
        lore.add(ColorUtil.c("&8Craft Light &7© Koruma Sistemi"));
        return lore;
    }

    /** Acik olan Koruma Bilgi menulerinin kafa isim/lore'unu animasyon fazina gore yeniler. */
    public void refreshAnimation(Player viewer, Inventory inv, double phase) {
        ItemStack head = inv.getItem(SLOT_HEAD);
        if (head == null || head.getType() != Material.PLAYER_HEAD) return;
        ItemMeta meta = head.getItemMeta();
        if (meta == null) return;

        boolean aktif = plugin.getProtectionManager().isActive(viewer.getUniqueId());
        int kalan = plugin.getProtectionManager().getRemaining(viewer.getUniqueId());
        int baslangic = plugin.getProtectionManager().getBaslangicBlok();

        meta.setDisplayName(headName(phase));
        meta.setLore(buildLore(aktif, kalan, baslangic, phase));
        head.setItemMeta(meta);
    }
}

