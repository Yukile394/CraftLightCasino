package com.craftlight.casino.casino;

import org.bukkit.Color;
import org.bukkit.Material;

public enum CasinoColor {

    KIRMIZI("§cKirmizi At", Material.RED_CONCRETE, Color.RED),
    SARI("§eSari At", Material.YELLOW_CONCRETE, Color.YELLOW),
    YESIL("§aYesil At", Material.LIME_CONCRETE, Color.LIME),
    MAVI("§9Mavi At", Material.BLUE_CONCRETE, Color.BLUE),
    MOR("§5Mor At", Material.PURPLE_CONCRETE, Color.PURPLE);

    private final String displayName;
    private final Material concrete;
    private final Color dye;

    CasinoColor(String displayName, Material concrete, Color dye) {
        this.displayName = displayName;
        this.concrete = concrete;
        this.dye = dye;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getConcrete() {
        return concrete;
    }

    public Color getDye() {
        return dye;
    }

    public static CasinoColor random() {
        CasinoColor[] values = values();
        return values[(int) (Math.random() * values.length)];
    }
}
