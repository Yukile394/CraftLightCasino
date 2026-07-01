package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.gui.MarketGUIHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LMarketItemYeriAyarlaCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public LMarketItemYeriAyarlaCommand(CasinoPlugin plugin) {
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
        player.openInventory(plugin.getMarketGUI().build(player, MarketGUIHolder.Mode.EDIT_POSITION));
        player.sendMessage("§e§l[Market] §eYer ayarlama modundasin. Bir esyaya tikla, sonra tasimak istedigin slota tikla.");
        return true;
    }
}
