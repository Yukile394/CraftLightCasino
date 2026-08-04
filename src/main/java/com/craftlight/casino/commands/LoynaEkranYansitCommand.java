package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoynaEkranYansitCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public LoynaEkranYansitCommand(CasinoPlugin plugin) {
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
        if (args.length < 2) {
            player.sendMessage(ColorUtil.c("&cKullanim: /loynaekranyansit <ID> <Isim>"));
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[0].replace("#", ""));
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtil.c("&cGecersiz ID! Sayi girmelisin."));
            return true;
        }

        String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        String error = plugin.getScreenManager().createAtLookedSurface(player, id, name);
        if (error != null) {
            player.sendMessage(ColorUtil.c("&c" + error));
            return true;
        }

        player.sendMessage(ColorUtil.c("&#4DA3FF&l[Ekran] &fSilvera ekrani &#4DA3FF#" + id + " &f(&#C0C0C0" + name + "&f) basariyla olusturuldu!"));
        player.sendMessage(ColorUtil.c("&#C0C0C0Ekranin karsisindaki 3x3 alanda durarak kontrolunu alabilirsin."));
        return true;
    }
}
