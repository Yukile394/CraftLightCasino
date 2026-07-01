package com.craftlight.casino;

import com.craftlight.casino.casino.CasinoGame;
import com.craftlight.casino.casino.CasinoManager;
import com.craftlight.casino.casino.CasinoSession;
import com.craftlight.casino.commands.*;
import com.craftlight.casino.economy.EconomyManager;
import com.craftlight.casino.gui.CasinoGUI;
import com.craftlight.casino.gui.LCoinGUI;
import com.craftlight.casino.gui.MarketGUI;
import com.craftlight.casino.hologram.HologramManager;
import com.craftlight.casino.listeners.ChatInputListener;
import com.craftlight.casino.listeners.GUIClickListener;
import com.craftlight.casino.market.MarketManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
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

    // /alanayarla icin "blok ayarla" bekleyen oyuncular -> hangi alan id
    private final Map<UUID, Integer> pendingBlockSelect = new ConcurrentHashMap<>();

    // Sohbetten bahis/fiyat miktari bekleyen oyuncular
    public enum ChatInputType { BAHIS_AYARLA, MARKET_FIYAT }
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
}
