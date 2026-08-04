package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AyarlaCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public AyarlaCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.c("&cBu komut sadece oyun icinde kullanilabilir."));
            return true;
        }
        if (!player.hasPermission("craftlight.admin")) {
            player.sendMessage(ColorUtil.c("&cBu komutu kullanma yetkin yok."));
            return true;
        }

        player.openInventory(plugin.getScreenAyarlaGUI().build(player));
        return true;
    }
}
