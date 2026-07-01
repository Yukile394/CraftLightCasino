package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LMarketEsyaEkleCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public LMarketEsyaEkleCommand(CasinoPlugin plugin) {
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
        if (args.length < 2) {
            player.sendMessage("§cKullanim: /lmarketesyaekle <isim> <#id>  §7(Elindeki esyayi ekler)");
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[args.length - 1].replace("#", ""));
        } catch (NumberFormatException e) {
            player.sendMessage("§cGecersiz ID! Sayi girmelisin. Kullanim: /lmarketesyaekle <isim> <#id>");
            return true;
        }

        String name = String.join(" ", java.util.Arrays.copyOfRange(args, 0, args.length - 1));

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType().isAir()) {
            player.sendMessage("§cElinde bos! Markete eklemek istedigin esyayi eline al.");
            return true;
        }

        if (plugin.getMarketManager().get(id) != null) {
            player.sendMessage("§cBu ID (#" + id + ") zaten kullaniliyor! Once /lmarketitemsil ile sil.");
            return true;
        }

        if (plugin.getMarketManager().getItems().size() >= plugin.getMarketManager().maxItem) {
            player.sendMessage("§cMarket dolu! En fazla " + plugin.getMarketManager().maxItem + " esya eklenebilir.");
            return true;
        }

        boolean added = plugin.getMarketManager().addItem(name, id, hand, 0);
        if (added) {
            player.sendMessage("§a§l[Market] §a'" + name + "' §aesyasi §e#" + id + " §aID'si ile markete eklendi!");
            player.sendMessage("§7Fiyat ayarlamayi unutma: §f/lmarketparaayarla " + name.replace(" ", "_") + " " + id + " <fiyat>");
        } else {
            player.sendMessage("§cEsya eklenemedi!");
        }
        return true;
    }
}
