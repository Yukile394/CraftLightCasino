package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LCoinParaCekCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public LCoinParaCekCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("craftlight.admin")) {
            sender.sendMessage("§cBu komutu kullanma yetkin yok.");
            return true;
        }
        if (!(sender instanceof Player executor)) {
            sender.sendMessage("§cBu komut sadece oyun icinde kullanilabilir (para senin hesabina eklenecegi icin).");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cKullanim: /lcoinparacek <miktar> <oyuncu>");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0].replace(",", "."));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cGecersiz miktar!");
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage("§cMiktar 0'dan buyuk olmalidir.");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cBu isimde bir oyuncu bulunamadi!");
            return true;
        }
        if (target.getUniqueId().equals(executor.getUniqueId())) {
            sender.sendMessage("§cKendinden para cekemezsin!");
            return true;
        }

        // Dupe onlemi: hedefin bakiyesinden fazlasi asla cekilmez, sadece elindeki kadar cekilir.
        double taken = plugin.getEconomyManager().withdrawUpTo(target.getUniqueId(), amount);
        if (taken <= 0) {
            sender.sendMessage("§c" + (target.getName() != null ? target.getName() : "Oyuncu") + " adli oyuncunun hic parasi yok, cekilemedi.");
            return true;
        }

        plugin.getEconomyManager().deposit(executor.getUniqueId(), taken);

        String currency = plugin.getEconomyManager().getCurrencyName();
        if (taken < amount) {
            sender.sendMessage("§e§l[LCoin] §e" + target.getName() + " adli oyuncunun yeterli parasi olmadigi icin sadece §6" + fmt(taken) + " " + currency + " §eciekildi.");
        } else {
            sender.sendMessage("§a§l[LCoin] §a" + target.getName() + " adli oyuncudan §e" + fmt(taken) + " " + currency + " §acekildi!");
        }

        if (target.isOnline()) {
            target.getPlayer().sendMessage("§c§l[LCoin] §c" + executor.getName() + " hesabindan §e" + fmt(taken) + " " + currency + " §cceki yapti!");
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
