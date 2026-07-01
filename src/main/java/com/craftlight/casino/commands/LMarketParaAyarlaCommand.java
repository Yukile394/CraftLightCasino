package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LMarketParaAyarlaCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public LMarketParaAyarlaCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("craftlight.admin")) {
            sender.sendMessage("§cBu komutu kullanma yetkin yok.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cKullanim: /lmarketparaayarla <isim> <#id> <fiyat>");
            return true;
        }

        double fiyat;
        int id;
        try {
            fiyat = Double.parseDouble(args[args.length - 1].replace(",", "."));
            id = Integer.parseInt(args[args.length - 2].replace("#", ""));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cKullanim: /lmarketparaayarla <isim> <#id> <fiyat>");
            return true;
        }

        if (fiyat < 0) {
            sender.sendMessage("§cFiyat negatif olamaz!");
            return true;
        }

        if (plugin.getMarketManager().get(id) == null) {
            sender.sendMessage("§c#" + id + " ID'li bir esya bulunamadi!");
            return true;
        }

        plugin.getMarketManager().setPrice(id, fiyat);
        sender.sendMessage("§a§l[Market] §a#" + id + " ID'li esyanin fiyati §6" + fmt(fiyat) + " " + plugin.getEconomyManager().getCurrencyName() + " §aolarak ayarlandi!");
        return true;
    }

    private String fmt(double val) {
        if (val == Math.floor(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }
}
