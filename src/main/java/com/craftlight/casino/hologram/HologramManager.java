package com.craftlight.casino.hologram;

import com.craftlight.casino.CasinoPlugin;
import com.craftlight.casino.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import net.kyori.adventure.text.Component;

import java.util.List;

/**
 * Harici bir hologram eklentisine ihtiyac duymadan, ArmorStand tabanli
 * cok satirli hologram olusturur. Standlar gorunmez, kucuk, yercekimsiz
 * ve etkilesimsizdir; sadece isim etiketleri (custom name) gorunur.
 */
public class HologramManager {

    private final CasinoPlugin plugin;
    private static final double LINE_GAP = 0.27;

    public HologramManager(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnHologram(Location baseLocation, List<String> lines) {
        Location current = baseLocation.clone();
        // En ustteki satir baseLocation olacak sekilde, alta dogru siralar
        for (String line : lines) {
            spawnLine(current.clone(), line);
            current.subtract(0, LINE_GAP, 0);
        }
    }

    private void spawnLine(Location location, String text) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setCollidable(false);
        stand.setCanTick(false);
        stand.setPersistent(true);
        stand.customName(Component.text(ColorUtil.c(text)));
        stand.setCustomNameVisible(true);
    }
}
