package com.craftlight.casino.util;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;

/**
 * Isim/lore metinlerine akan (animasyonlu) RGB gradyan rengi uygulamak icin yardimci sinif.
 * "phase" degeri 0.0-1.0 arasinda bir kaydirma degeridir; zamanla (bir BukkitRunnable ile)
 * arttirilip/dongusel sarilirsa renklerin surekli "aktigi" bir efekt olusur.
 * Kalin (bold) KULLANILMAZ, sadece hex renk kodlari uygulanir.
 */
public final class GradientUtil {

    private GradientUtil() {
    }

    /**
     * Verilen duz metni (zaten renk kodu icermemeli), verilen renk paleti arasinda
     * dongusel bir gradyanla boyar. Metnin her karakteri, phase'e gore kaydirilmis
     * bir konumdan renk alir; boylece ust uste cagrilarda phase arttikca renkler
     * metin uzerinde "akiyormus" gibi gorunur.
     */
    public static String flow(String text, double phase, Color... palette) {
        if (text == null || text.isEmpty() || palette.length == 0) return text;
        String clean = org.bukkit.ChatColor.stripColor(ColorUtil.c(text));
        if (clean == null) clean = text;
        int len = clean.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char ch = clean.charAt(i);
            double pos = ((double) i / Math.max(1, len - 1)) + phase;
            pos = pos - Math.floor(pos); // 0.0 - 1.0 araligina sarar (dongusel akis)
            Color color = sampleGradient(pos, palette);
            sb.append(hex(color)).append(ch);
        }
        return sb.toString();
    }

    private static Color sampleGradient(double pos, Color[] palette) {
        if (palette.length == 1) return palette[0];
        double scaled = pos * palette.length;
        int i1 = ((int) Math.floor(scaled)) % palette.length;
        int i2 = (i1 + 1) % palette.length;
        double ratio = scaled - Math.floor(scaled);
        Color c1 = palette[i1];
        Color c2 = palette[i2];
        int r = (int) (c1.getRed() + ratio * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + ratio * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + ratio * (c2.getBlue() - c1.getBlue()));
        return new Color(r, g, b);
    }

    private static String hex(Color color) {
        String h = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        return ChatColor.of(h).toString();
    }

    // Hazir paletler
    public static final Color[] RAINBOW = {
            new Color(0xFF4B4B), new Color(0xFFA84B), new Color(0xFFF04B),
            new Color(0x63FF4B), new Color(0x4BFFE3), new Color(0x4B7BFF),
            new Color(0xB44BFF), new Color(0xFF4BD8), new Color(0xFF4B4B)
    };

    public static final Color[] PINK_WHITE = {
            new Color(0xFF6FCF), new Color(0xFFFFFF), new Color(0xFF9AE0), new Color(0xFFFFFF)
    };

    public static final Color[] RED_ORANGE = {
            new Color(0xFF3B3B), new Color(0xFF9A3B), new Color(0xFFD23B), new Color(0xFF3B3B)
    };
      }
  
