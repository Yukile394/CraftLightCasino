package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GazinoHologramCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public GazinoHologramCommand(CasinoPlugin plugin) {
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

        List<String> lines = List.of(
                "&6&l✦ CRAFT LIGHT ✦",
                "&e&lG A Z İ N O",
                "&7&m                         ",
                "&f5 renkten birini sec, bahsini",
                "&fyap ve sansini dene!",
                "&aKazanirsan bahsin &6x2 &akatlanir!",
                "&7&m                         ",
                "&bBaslamak icin: &f/loyna <id>",
                "&8Craft Light Sunucusu"
        );

        plugin.getHologramManager().spawnHologram(player.getLocation().add(0, 2.2, 0), lines);
        player.sendMessage("§a§l[Gazino] §aHologram basariyla olusturuldu!");
        return true;
    }
}
