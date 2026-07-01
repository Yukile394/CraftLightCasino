package com.craftlight.casino.commands;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.casino.CasinoArea;
import com.craftlight.casino.casino.CasinoSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoynaCommand implements CommandExecutor {

    private final CasinoPlugin plugin;

    public LoynaCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cBu komut sadece oyun icinde kullanilabilir.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cKullanim: /loyna <id>  veya  /loyna <bahis> <id>");
            return true;
        }

        int id;
        double bahis = 0;

        try {
            if (args.length == 1) {
                id = Integer.parseInt(args[0].replace("#", ""));
            } else {
                // /loyna <bahis> <#id>
                bahis = Double.parseDouble(args[0].replace(",", "."));
                id = Integer.parseInt(args[1].replace("#", ""));
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cGecersiz sayi girdin! Kullanim: /loyna <id>  veya  /loyna <bahis> <#id>");
            return true;
        }

        if (!plugin.getCasinoManager().isValidId(id)) {
            player.sendMessage("§cGecersiz gazino ID! (1-" + plugin.getCasinoManager().maxAlan + " arasi olmali)");
            return true;
        }

        CasinoArea area = plugin.getCasinoManager().get(id);
        if (area == null || !area.isFullyConfigured()) {
            player.sendMessage("§cGazino #" + id + " henuz tam olarak ayarlanmamis!");
            return true;
        }

        if (area.isOccupied() && !player.getUniqueId().equals(area.getActivePlayer())) {
            player.sendMessage("§c§l[Gazino #" + id + "] §cBu gazinoda su anda baska biri duruyor! §7Diger gazinolara goz at.");
            return true;
        }

        if (!area.isPlayerInside(player.getLocation())) {
            player.sendMessage("§c§l[Gazino #" + id + "] §cBu gazinoyu kullanmak icin gazinonun uzerinde durmalisin!");
            return true;
        }

        CasinoSession session = plugin.getOrCreateSession(player, id);
        area.setActivePlayer(player.getUniqueId());

        if (bahis > 0) {
            double min = plugin.getConfig().getDouble("casino.min-bahis", 100);
            double max = plugin.getConfig().getDouble("casino.max-bahis", 1000000);
            if (bahis < min) {
                player.sendMessage("§cMinimum bahis miktari: §e" + fmt(min) + " " + plugin.getEconomyManager().getCurrencyName());
                return true;
            }
            if (session.getBet() + bahis > max) {
                player.sendMessage("§cMaksimum bahis miktarini asamazsin! (Maks: " + fmt(max) + ")");
                return true;
            }
            if (!plugin.getEconomyManager().withdraw(player.getUniqueId(), bahis)) {
                player.sendMessage("§cYetersiz bakiye! Bu kadar " + plugin.getEconomyManager().getCurrencyName() + "'in yok.");
                return true;
            }
            session.setBet(session.getBet() + bahis);
            player.sendMessage("§a§l[Gazino] §aBahsine §e" + fmt(bahis) + " " + plugin.getEconomyManager().getCurrencyName() + " §aeklendi! Toplam bahis: §6" + fmt(session.getBet()));
        }

        player.openInventory(plugin.getCasinoGUI().build(player, area, session, null));
        return true;
    }

    private String fmt(double val) {
        if (val == Math.floor(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }
}
