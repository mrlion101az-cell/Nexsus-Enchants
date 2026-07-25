package com.nexusuniverse.enchants.pumpkin;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * All three positive enchants work by cancelling a mob's targeting
 * attempt via the standard EntityTargetEvent -- they can only stop a
 * mob from starting to hunt you, not make an already-hunting mob give
 * up mid-chase (Bukkit doesn't expose "un-target" cleanly outside of
 * this event).
 */
public class PumpkinEnchantListener implements Listener {

    private static final double FALSE_FACE_RANGE = 8.0;

    private final EnchantRegistry registry;
    private final StillnessTracker stillnessTracker;
    private final Random random = new Random();

    public PumpkinEnchantListener(EnchantRegistry registry, StillnessTracker stillnessTracker) {
        this.registry = registry;
        this.stillnessTracker = stillnessTracker;
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity hunter)) return;

        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null || helmet.getType() != Material.CARVED_PUMPKIN) return;

        int scarecrow = registry.getLevel(helmet, "scarecrow");
        if (scarecrow > 0 && stillnessTracker.hasBeenStill(player)) {
            event.setCancelled(true);
            return;
        }

        int shrouded = registry.getLevel(helmet, "shrouded");
        if (shrouded > 0 && random.nextDouble() < 0.15 * shrouded) {
            event.setCancelled(true);
            return;
        }

        int falseFace = registry.getLevel(helmet, "false_face");
        if (falseFace > 0 && hunter.getLocation().distance(player.getLocation()) > FALSE_FACE_RANGE
                && random.nextDouble() < 0.30 * falseFace) {
            event.setCancelled(true);
            return;
        }

        int gourdWard = registry.getLevel(helmet, "gourd_ward");
        if (gourdWard > 0 && hunter.getType() == org.bukkit.entity.EntityType.PIGLIN) {
            event.setCancelled(true);
        }
    }
}
