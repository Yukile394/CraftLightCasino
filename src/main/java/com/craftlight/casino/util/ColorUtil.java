package com.craftlight.casino.util;

import net.md_5.bungee.api.ChatColor;

public final class ColorUtil {

    private ColorUtil() {
    }

    /**
     * '&' renk kodlarini ve HEX (&#RRGGBB) kodlarini normal Minecraft renklerine cevirir.
     */
    public static String c(String input) {
        if (input == null) return "";
        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length && chars[i + 1] == '#' && i + 7 < chars.length) {
                String hex = input.substring(i + 2, i + 8);
                if (hex.matches("[0-9a-fA-F]{6}")) {
                    try {
                        result.append(ChatColor.of("#" + hex));
                        i += 7;
                        continue;
                    } catch (Exception ignored) {
                    }
                }
            }
            result.append(chars[i]);
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', result.toString());
    }
}
