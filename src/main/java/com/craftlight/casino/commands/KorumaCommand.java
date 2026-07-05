package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KorumaCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public KorumaCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cBu komut sadece oyun icinde kullanilabilir.");
            return true;
        }
        player.openInventory(plugin.getKorumaGUI().build(player));
        return true;
    }
}

