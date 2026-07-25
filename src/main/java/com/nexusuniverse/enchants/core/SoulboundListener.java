package com.nexusuniverse.enchants.core;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Soulbound items are pulled out of the death drop list and handed back
 * on respawn, instead of trying to prevent the drop entirely (which
 * fights with how PlayerDeathEvent's drop list already works). If the
 * inventory is somehow full on respawn (shouldn't normally happen right
 * after death, but just in case), leftover items drop at the player's
 * feet instead of vanishing.
 *
 * Keepsake works the same way, but is skipped entirely on a PvP death
 * (event.getEntity().getKiller() != null) -- it's the "protects your
 * stuff from bad luck, not from a fair PvP loss" version of the same idea.
 */
public class SoulboundListener implements Listener {

    private final EnchantRegistry registry;
    private final Map<UUID, List<ItemStack>> heldForRespawn = new HashMap<>();

    public SoulboundListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        boolean pvpDeath = event.getEntity().getKiller() != null;
        List<ItemStack> drops = event.getDrops();
        List<ItemStack> kept = new ArrayList<>();

        drops.removeIf(item -> {
            if (registry.getLevel(item, "soulbound") > 0) {
                kept.add(item);
                return true;
            }
            if (!pvpDeath && registry.getLevel(item, "keepsake") > 0) {
                kept.add(item);
                return true;
            }
            return false;
        });

        if (!kept.isEmpty()) {
            heldForRespawn.put(player.getUniqueId(), kept);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<ItemStack> kept = heldForRespawn.remove(player.getUniqueId());
        if (kept == null) return;

        for (ItemStack item : kept) {
            var leftover = player.getInventory().addItem(item);
            leftover.values().forEach(overflow -> player.getWorld().dropItemNaturally(player.getLocation(), overflow));
        }
    }
}
