package com.craftlight.casino.listeners;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.casino.CasinoArea;
import com.craftlight.casino.casino.CasinoSession;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatInputListener implements Listener {

    private final CasinoPlugin plugin;

    public ChatInputListener(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        Player player = e.getPlayer();
        CasinoPlugin.ChatInputRequest request = plugin.getChatInputWaiters().get(player.getUniqueId());
        if (request == null) return;

        e.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(e.message()).trim();
        plugin.getChatInputWaiters().remove(player.getUniqueId());

        Bukkit.getScheduler().runTask(plugin, () -> handleInput(player, request, message));
    }

    private void handleInput(Player player, CasinoPlugin.ChatInputRequest request, String message) {
        if (message.equalsIgnoreCase("iptal")) {
            player.sendMessage("§cIslem iptal edildi.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(message.replace(",", ".").trim());
        } catch (NumberFormatException ex) {
            player.sendMessage("§cGecerli bir sayi girmedin! Islem iptal edildi.");
            return;
        }

        if (amount <= 0) {
            player.sendMessage("§cMiktar 0'dan buyuk olmalidir. Islem iptal edildi.");
            return;
        }

        switch (request.type()) {
            case BAHIS_AYARLA -> handleBahisAyarla(player, request.contextId(), amount);
            case MARKET_FIYAT -> {
                // Su an /lmarketparaayarla komut ile dogrudan yapiliyor, ileride sohbet destekli
                // fiyat girisi icin ayrilmistir.
            }
        }
    }

    private void handleBahisAyarla(Player player, int areaId, double amount) {
        CasinoArea area = plugin.getCasinoManager().get(areaId);
        CasinoSession session = plugin.getSession(player.getUniqueId());
        if (area == null || session == null) {
            player.sendMessage("§cBir hata olustu, gazino oturumun bulunamadi. Tekrar /loyna " + areaId + " yaz.");
            return;
        }

        double min = plugin.getConfig().getDouble("casino.min-bahis", 100);
        double max = plugin.getConfig().getDouble("casino.max-bahis", 1000000);

        if (amount < min) {
            player.sendMessage("§cMinimum bahis miktari: §e" + fmt(min) + " " + plugin.getEconomyManager().getCurrencyName());
            return;
        }
        if (amount > max) {
            player.sendMessage("§cMaksimum bahis miktari: §e" + fmt(max) + " " + plugin.getEconomyManager().getCurrencyName());
            return;
        }

        if (!plugin.getEconomyManager().withdraw(player.getUniqueId(), amount)) {
            player.sendMessage("§cYetersiz bakiye! Bu kadar " + plugin.getEconomyManager().getCurrencyName() + "'in yok.");
            return;
        }

        session.setBet(session.getBet() + amount);
        player.sendMessage("§a§l[Gazino] §aBahsine §e" + fmt(amount) + " " + plugin.getEconomyManager().getCurrencyName() + " §aeklendi! Toplam: §6" + fmt(session.getBet()));
        player.openInventory(plugin.getCasinoGUI().build(player, area, session, session.getPendingColor()));
    }

    private String fmt(double val) {
        if (val == Math.floor(val)) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }
}
