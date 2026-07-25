package com.nexusuniverse.enchants.core;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

/**
 * Kept separate from the category-specific listeners (ToolEnchantListener,
 * ElytraEnchantListener, FishingEnchantListener each have their own
 * PlayerItemDamageEvent handler for their own category-scoped enchants) --
 * this one is deliberately not scoped to any material, since Ageless and
 * Spare Parts can go on anything.
 */
public class UniversalEnchantListener implements Listener {

    private final EnchantRegistry registry;

    public UniversalEnchantListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        int ageless = registry.getLevel(item, "ageless");
        if (ageless == 0) return;

        if (!(item.getItemMeta() instanceof Damageable damageable)) return;
        int maxDurability = item.getType().getMaxDurability();
        if (maxDurability <= 0) return;

        int resultingDamage = damageable.getDamage() + event.getDamage();
        if (resultingDamage >= maxDurability) {
            event.setDamage(Math.max(0, maxDurability - damageable.getDamage() - 1));
        }
    }

    @EventHandler
    public void onItemEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;

        if (registry.getLevel(item.getItemStack(), "spare_parts") > 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (registry.getLevel(event.getEntity().getItemStack(), "everlasting") > 0) {
            event.setCancelled(true);
        }
    }
}
