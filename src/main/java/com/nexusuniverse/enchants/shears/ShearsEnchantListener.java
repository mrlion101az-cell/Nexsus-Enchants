package com.nexusuniverse.enchants.shears;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Scoped to sheep only for v1, not every shearable entity (mushroom
 * cows, snow golems, beehives). Sheep have simple, well-established
 * Bukkit API (isSheared()/setSheared(boolean), DyeColor) that this can
 * build on reliably; extending to the other shearable types is a real
 * follow-up, not attempted here to avoid leaning on less-certain API
 * surface for the first version.
 */
public class ShearsEnchantListener implements Listener {

    private final EnchantRegistry registry;
    private final Random random = new Random();

    public ShearsEnchantListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (event.getItem().getType() != Material.SHEARS) return;
        if (registry.getLevel(event.getItem(), "quick_clip") > 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {
        if (!(event.getEntity() instanceof Sheep sheared)) return;

        Player player = event.getPlayer();
        ItemStack shears = player.getInventory().getItemInMainHand();

        int bulkShear = registry.getLevel(shears, "bulk_shear");
        if (bulkShear > 0) {
            for (Entity nearby : sheared.getNearbyEntities(4, 4, 4)) {
                if (nearby instanceof Sheep sheep && !sheep.isSheared()) {
                    sheep.setSheared(true);
                    sheep.getWorld().dropItemNaturally(sheep.getLocation(), new ItemStack(woolMaterialFor(sheep)));
                }
            }
        }

        int bountiful = registry.getLevel(shears, "bountiful_shear");
        if (bountiful > 0 && random.nextDouble() < 0.20 * bountiful) {
            sheared.getWorld().dropItemNaturally(sheared.getLocation(), new ItemStack(woolMaterialFor(sheared)));
        }
    }

    private Material woolMaterialFor(Sheep sheep) {
        return Material.valueOf(sheep.getColor().name() + "_WOOL");
    }
}
