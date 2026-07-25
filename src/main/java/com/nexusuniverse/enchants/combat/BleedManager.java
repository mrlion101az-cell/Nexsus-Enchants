package com.nexusuniverse.enchants.combat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks active bleed stacks and ticks them once per second from the
 * central loop. Damage is applied with no attacker attribution (Bukkit's
 * plain damage() call doesn't carry a damager) -- so it won't show up as
 * a specific player's kill in death messages/stats. Worth knowing if
 * you're tracking PvP kill stats elsewhere.
 */
public class BleedManager {

    private static final int DURATION_SECONDS = 5;

    private final Map<UUID, Integer> secondsRemaining = new HashMap<>();
    private final Map<UUID, Integer> level = new HashMap<>();

    public void applyBleed(LivingEntity victim, int bleedLevel) {
        secondsRemaining.put(victim.getUniqueId(), DURATION_SECONDS);
        level.put(victim.getUniqueId(), bleedLevel);
    }

    /** Called once per second from the central tick loop. */
    public void tick() {
        Iterator<Map.Entry<UUID, Integer>> it = secondsRemaining.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            UUID id = entry.getKey();

            Entity entity = Bukkit.getEntity(id);
            if (entity instanceof LivingEntity living && living.isValid() && !living.isDead()) {
                living.damage(0.5 * level.getOrDefault(id, 1));
            }

            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                it.remove();
                level.remove(id);
            } else {
                entry.setValue(remaining);
            }
        }
    }
}
