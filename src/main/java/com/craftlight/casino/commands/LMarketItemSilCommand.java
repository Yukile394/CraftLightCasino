package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LMarketItemSilCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public LMarketItemSilCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("craftlight.admin")) {
            sender.sendMessage("§cBu komutu kullanma yetkin yok.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cKullanim: /lmarketitemsil <isim> <#id>");
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[args.length - 1].replace("#", ""));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cGecersiz ID! Sayi girmelisin.");
            return true;
        }

        if (plugin.getMarketManager().get(id) == null) {
            sender.sendMessage("§c#" + id + " ID'li bir esya bulunamadi!");
            return true;
        }

        boolean removed = plugin.getMarketManager().removeItem(id);
        if (removed) {
            sender.sendMessage("§a§l[Market] §a#" + id + " ID'li esya market'ten silindi!");
        } else {
            sender.sendMessage("§cEsya silinemedi!");
        }
        return true;
    }
}
