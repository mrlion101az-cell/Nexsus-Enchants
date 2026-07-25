package com.nexusuniverse.enchants.combat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RampageManager {

    private static final long WINDOW_MS = 8_000; // must land another kill within 8s to keep/build the streak

    private final Map<UUID, Integer> stacks = new HashMap<>();
    private final Map<UUID, Long> lastKill = new HashMap<>();

    public void onKill(Player player, int maxStacks) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long last = lastKill.get(id);
        int current = (last != null && now - last <= WINDOW_MS) ? stacks.getOrDefault(id, 0) : 0;
        stacks.put(id, Math.min(maxStacks, current + 1));
        lastKill.put(id, now);
    }

    public int getStacks(Player player) {
        UUID id = player.getUniqueId();
        Long last = lastKill.get(id);
        if (last == null || System.currentTimeMillis() - last > WINDOW_MS) return 0;
        return stacks.getOrDefault(id, 0);
    }
}
