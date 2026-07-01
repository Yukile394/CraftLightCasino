package com.craftlight.casino.listeners;

import com.craftlight.casino.CasinoPlugin;
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
            case MARKET_FIYAT -> {
                // Su an /lmarketparaayarla komut ile dogrudan yapiliyor, ileride sohbet destekli
                // fiyat girisi icin ayrilmistir.
            }
        }
    }
}
