package com.craftlight.casino;

import com.craftlight.casino.casino.CasinoGame;
import com.craftlight.casino.casino.CasinoManager;
import com.craftlight.casino.casino.CasinoSession;
import com.craftlight.casino.commands.*;
import com.craftlight.casino.economy.EconomyManager;
import com.craftlight.casino.gui.CasinoGUI;
import com.craftlight.casino.gui.LCoinGUI;
import com.craftlight.casino.gui.MarketGUI;
import com.craftlight.casino.gui.MarketGUIHolder;
import com.craftlight.casino.hologram.HologramManager;
import com.craftlight.casino.listeners.ChatInputListener;
import com.craftlight.casino.listeners.GUIClickListener;
import com.craftlight.casino.market.MarketManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CasinoPlugin extends JavaPlugin {

    private EconomyManager economyManager;
    private CasinoManager casinoManager;
    private MarketManager marketManager;
    private HologramManager hologramManager;
    private CasinoGame casinoGame;

    private CasinoGUI casinoGUI;
    private MarketGUI marketGUI;
    private LCoinGUI lcoinGUI;

    // Oyuncu adina aktif gazino oturumlari (alan#id -> session)
    private final Map<UUID, CasinoSession> sessions = new ConcurrentHashMap<>();

    // GUI'yi programatik olarak yeniden acarken (at secimi vb.) tetiklenen
    // sahte InventoryCloseEvent'i yok saymak icin kullanilir.
    private final Set<UUID> suppressNextClose = ConcurrentHashMap.newKeySet();

    // /alanayarla icin "blok ayarla" bekleyen oyuncular -> hangi alan id
    private final Map<UUID, Integer> pendingBlockSelect = new ConcurrentHashMap<>();

    // Sohbetten bahis/fiyat miktari bekleyen oyuncular
    public enum ChatInputType { MARKET_FIYAT }
    public record ChatInputRequest(ChatInputType type, int contextId) {}
    private final Map<UUID, ChatInputRequest> chatInputWaiters = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.economyManager = new EconomyManager(this);
        this.casinoManager = new CasinoManager(this);
        this.marketManager = new MarketManager(this);
        this.hologramManager = new HologramManager(this);
        this.casinoGame = new CasinoGame(this);

        this.casinoGUI = new CasinoGUI(this);
        this.marketGUI = new MarketGUI(this);
        this.lcoinGUI = new LCoinGUI(this);

        getServer().getPluginManager().registerEvents(new GUIClickListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);

        getCommand("alanayarla").setExecutor(new AlanAyarlaCommand(this));
        getCommand("loyna").setExecutor(new LoynaCommand(this));
        getCommand("lcoin").setExecutor(new LCoinCommand(this));
        getCommand("lcoinver").setExecutor(new LCoinVerCommand(this));
        getCommand("lcoinparacek").setExecutor(new LCoinParaCekCommand(this));
        getCommand("gazinohologram").setExecutor(new GazinoHologramCommand(this));
        getCommand("lmarket").setExecutor(new LMarketCommand(this));
        getCommand("lmarketesyaekle").setExecutor(new LMarketEsyaEkleCommand(this));
        getCommand("lmarketitemsil").setExecutor(new LMarketItemSilCommand(this));
        getCommand("lmarketitemyeriayarla").setExecutor(new LMarketItemYeriAyarlaCommand(this));
        getCommand("lmarketparaayarla").setExecutor(new LMarketParaAyarlaCommand(this));

        // Market GUI'sinde item isimlerini/lorelerini akan (RGB flowing) renklerle guncelleyen gorev
        new BukkitRunnable() {
            double phase = 0.0;

            @Override
            public void run() {
                phase += 0.015;
                if (phase >= 1.0) phase -= 1.0;
                for (Player p : getServer().getOnlinePlayers()) {
                    InventoryView view = p.getOpenInventory();
                    if (view == null) continue;
                    if (!(view.getTopInventory().getHolder() instanceof MarketGUIHolder holder)) continue;
                    marketGUI.refreshAnimation(p, view.getTopInventory(), holder.getMode(), phase);
                }
            }
        }.runTaskTimer(this, 0L, 2L);

        getLogger().info("CraftLightCasino basariyla etkinlestirildi!");
    }

    @Override
    public void onDisable() {
        if (casinoManager != null) casinoManager.save();
        if (marketManager != null) marketManager.save();
        getLogger().info("CraftLightCasino devre disi birakildi.");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public CasinoManager getCasinoManager() {
        return casinoManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public CasinoGame getCasinoGame() {
        return casinoGame;
    }

    public CasinoGUI getCasinoGUI() {
        return casinoGUI;
    }

    public MarketGUI getMarketGUI() {
        return marketGUI;
    }

    public LCoinGUI getLcoinGUI() {
        return lcoinGUI;
    }

    public CasinoSession getOrCreateSession(Player player, int areaId) {
        return sessions.computeIfAbsent(player.getUniqueId(), u -> new CasinoSession(u, areaId));
    }

    public CasinoSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public void clearSession(UUID uuid) {
        sessions.remove(uuid);
    }

    public Map<UUID, Integer> getPendingBlockSelect() {
        return pendingBlockSelect;
    }

    public Map<UUID, ChatInputRequest> getChatInputWaiters() {
        return chatInputWaiters;
    }

    /**
     * Gazino GUI'sini "sessizce" yeniden acar: bu esnada olusan InventoryCloseEvent
     * gercek bir kapatma olarak islenmez (bahis iade edilmez, secim sifirlanmaz).
     * At secimi, bahis geri cekme gibi ic guncellemelerde kullanilir.
     */
    public void openInventorySilently(Player player, Inventory inv) {
        suppressNextClose.add(player.getUniqueId());
        player.openInventory(inv);
        suppressNextClose.remove(player.getUniqueId());
    }

    public boolean consumeSuppressedClose(UUID uuid) {
        return suppressNextClose.remove(uuid);
    }
}
