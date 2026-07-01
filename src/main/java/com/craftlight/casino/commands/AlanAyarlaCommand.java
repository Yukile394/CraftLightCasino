package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.casino.CasinoArea;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AlanAyarlaCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public AlanAyarlaCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cBu komut sadece oyun icinde kullanilabilir.");
            return true;
        }
        if (!player.hasPermission("craftlight.admin")) {
            player.sendMessage("§cBu komutu kullanma yetkin yok.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cKullanim: /alanayarla <id 1-" + plugin.getCasinoManager().maxAlan + "> <1|2|blok>");
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[0].replace("#", ""));
        } catch (NumberFormatException e) {
            player.sendMessage("§cGecersiz ID! Sayi girmelisin.");
            return true;
        }

        if (!plugin.getCasinoManager().isValidId(id)) {
            player.sendMessage("§cID 1 ile " + plugin.getCasinoManager().maxAlan + " arasinda olmalidir.");
            return true;
        }

        CasinoArea area = plugin.getCasinoManager().getOrCreate(id);
        String type = args[1].toLowerCase();

        switch (type) {
            case "1" -> {
                area.setPos1(player.getLocation());
                player.sendMessage("§a§l[Gazino] §aAlan #" + id + " icin 1. nokta ayarlandi!");
            }
            case "2" -> {
                area.setPos2(player.getLocation());
                player.sendMessage("§a§l[Gazino] §aAlan #" + id + " icin 2. nokta ayarlandi!");
            }
            case "blok", "block" -> {
                Block target = player.getTargetBlockExact(6);
                if (target == null || target.getType().isAir()) {
                    player.sendMessage("§cBir bloga bakmiyorsun! Renkli beton blogunun uzerine bak ve tekrar dene.");
                    return true;
                }
                area.setBlockLoc(target.getLocation());
                player.sendMessage("§a§l[Gazino] §aAlan #" + id + " icin degisen blok konumu ayarlandi: §e" + target.getX() + ", " + target.getY() + ", " + target.getZ());
            }
            default -> {
                player.sendMessage("§cKullanim: /alanayarla <id> <1|2|blok>");
                return true;
            }
        }

        plugin.getCasinoManager().save();

        if (area.isFullyConfigured()) {
            player.sendMessage("§b§l[Gazino] §bAlan #" + id + " tamamen ayarlandi ve kullanima hazir! §7(/loyna " + id + ")");
        } else {
            player.sendMessage("§7Eksik ayarlar: " +
                    (area.getPos1() == null ? "§c1.Nokta " : "§a1.Nokta ") +
                    (area.getPos2() == null ? "§c2.Nokta " : "§a2.Nokta ") +
                    (area.getBlockLoc() == null ? "§cBlok" : "§aBlok"));
        }

        return true;
    }
}
