package com.nexusuniverse.enchants.combat;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class OpeningStrikeTracker {

    private static final long FRESH_WINDOW_MS = 10_000;

    private final Map<String, Long> lastHit = new HashMap<>();

    /** Returns true if this is the first hit on this target in the last 10s, and records this hit either way. */
    public boolean isFreshTarget(Player attacker, LivingEntity victim) {
        String key = attacker.getUniqueId() + ":" + victim.getUniqueId();
        long now = System.currentTimeMillis();
        Long last = lastHit.get(key);
        lastHit.put(key, now);
        return last == null || now - last > FRESH_WINDOW_MS;
    }
}
