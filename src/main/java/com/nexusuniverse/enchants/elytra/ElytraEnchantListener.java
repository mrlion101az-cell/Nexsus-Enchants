package com.nexusuniverse.enchants.elytra;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Safe Landing is deliberately scoped tighter than the existing
 * Featherfall+ armor enchant: it only protects a fall that happened
 * shortly (3s) after gliding stopped -- a "you just crash-landed"
 * safety net, not blanket fall-damage immunity. Bukkit doesn't expose
 * "was recently gliding" directly, so this tracks it manually via
 * EntityToggleGlideEvent.
 */
public class ElytraEnchantListener implements Listener {

    private static final long SAFE_LANDING_WINDOW_MS = 3_000;

    private final EnchantRegistry registry;
    private final Map<UUID, Long> lastGlideEnd = new HashMap<>();
    private final Random random = new Random();

    public ElytraEnchantListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return; // fires for any gliding entity, not just players
        if (!event.isGliding()) { // gliding just stopped
            lastGlideEnd.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        var elytra = player.getInventory().getChestplate();
        if (registry.getLevel(elytra, "safe_landing") == 0) return;

        Long ended = lastGlideEnd.get(player.getUniqueId());
        if (ended != null && System.currentTimeMillis() - ended <= SAFE_LANDING_WINDOW_MS) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (event.getItem().getType() != Material.ELYTRA) return;

        int featherlight = registry.getLevel(event.getItem(), "featherlight");
        int brittleWings = registry.getLevel(event.getItem(), "curse_of_brittle_wings");
        int reinforcedWings = registry.getLevel(event.getItem(), "reinforced_wings");
        if (featherlight == 0 && brittleWings == 0 && reinforcedWings == 0) return;

        if (reinforcedWings > 0 && random.nextDouble() < 0.10 * reinforcedWings) {
            event.setDamage(0);
            return;
        }

        int adjusted = event.getDamage();
        if (featherlight > 0) {
            adjusted = Math.max(0, adjusted - featherlight); // loses less durability per level
        }
        if (brittleWings > 0) {
            adjusted += brittleWings; // loses more durability per level
        }
        event.setDamage(adjusted);
    }
}
