package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LCoinVerCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public LCoinVerCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("craftlight.admin")) {
            sender.sendMessage("§cBu komutu kullanma yetkin yok.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cKullanim: /lcoinver <oyuncu> <miktar>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cBu isimde bir oyuncu bulunamadi!");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1].replace(",", "."));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cGecersiz miktar!");
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("§cMiktar 0'dan buyuk olmalidir.");
            return true;
        }

        plugin.getEconomyManager().deposit(target.getUniqueId(), amount);
        String currency = plugin.getEconomyManager().getCurrencyName();
        sender.sendMessage("§a§l[LCoin] §a" + target.getName() + " adli oyuncuya §e" + fmt(amount) + " " + currency + " §averildi!");
        if (target.isOnline()) {
            target.getPlayer().sendMessage("§a§l[LCoin] §aHesabina §e" + fmt(amount) + " " + currency + " §aeklendi!");
        }
        return true;
    }

    private String fmt(double val) {
        if (val == Math.floor(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }
}
