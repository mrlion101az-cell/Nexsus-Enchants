package com.nexusuniverse.enchants.compass;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Pathfinder and Curse of the Wanderer are both continuous ("always
 * pointing at X") so they live in PassiveEffectManager's once-per-second
 * tick alongside everything else like that. Homeward is a one-shot
 * action instead, so it gets its own small interact listener here.
 */
public class CompassEnchantListener implements Listener {

    private final EnchantRegistry registry;
    private final java.util.Map<java.util.UUID, Boolean> twinSignalShowingSpawn = new java.util.HashMap<>();

    public CompassEnchantListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack compass = player.getInventory().getItemInMainHand();

        if (player.isSneaking()) {
            int homeward = registry.getLevel(compass, "homeward");
            if (homeward == 0) return;

            Location bed = player.getBedSpawnLocation();
            Location target = bed != null ? bed : player.getWorld().getSpawnLocation();
            player.setCompassTarget(target);
            player.sendMessage(bed != null
                    ? "§bCompass now points to your bed."
                    : "§bNo bed set -- compass points to world spawn instead.");
            return;
        }

        int twinSignal = registry.getLevel(compass, "twin_signal");
        if (twinSignal == 0) return;

        boolean showingSpawn = twinSignalShowingSpawn.getOrDefault(player.getUniqueId(), false);
        if (showingSpawn) {
            Player nearest = null;
            double nearestDistanceSquared = Double.MAX_VALUE;
            for (Player candidate : player.getWorld().getPlayers()) {
                if (candidate.equals(player)) continue;
                double distanceSquared = candidate.getLocation().distanceSquared(player.getLocation());
                if (distanceSquared < nearestDistanceSquared) {
                    nearestDistanceSquared = distanceSquared;
                    nearest = candidate;
                }
            }
            if (nearest != null) {
                player.setCompassTarget(nearest.getLocation());
                player.sendMessage("§eCompass now points to the nearest player.");
            } else {
                player.sendMessage("§cNo other players nearby to point to.");
                return;
            }
        } else {
            player.setCompassTarget(player.getWorld().getSpawnLocation());
            player.sendMessage("§eCompass now points to world spawn.");
        }
        twinSignalShowingSpawn.put(player.getUniqueId(), !showingSpawn);
    }
}
