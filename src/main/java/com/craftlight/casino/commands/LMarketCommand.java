package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.gui.MarketGUIHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LMarketCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public LMarketCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cBu komut sadece oyun icinde kullanilabilir.");
            return true;
        }
        player.openInventory(plugin.getMarketGUI().build(player, MarketGUIHolder.Mode.VIEW));
        return true;
    }
}
