package com.nexusuniverse.enchants.combat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SecondWindManager {

    private static final long COOLDOWN_MS = 60_000; // 1 minute

    private final Map<UUID, Long> lastTrigger = new HashMap<>();

    /** Returns true (and starts the cooldown) if this player's Second Wind is off cooldown right now. */
    public boolean tryTrigger(Player player) {
        long now = System.currentTimeMillis();
        Long last = lastTrigger.get(player.getUniqueId());
        if (last != null && now - last < COOLDOWN_MS) return false;
        lastTrigger.put(player.getUniqueId(), now);
        return true;
    }
}
