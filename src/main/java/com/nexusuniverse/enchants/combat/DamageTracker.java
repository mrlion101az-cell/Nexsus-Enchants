package com.nexusuniverse.enchants.combat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Shared between ArmorDefenseListener (records) and WeaponEnchantListener (reads), for Adrenaline. */
public class DamageTracker {

    private static final long WINDOW_MS = 5_000;

    private final Map<UUID, Long> lastDamagedAt = new HashMap<>();

    public void recordDamage(Player player) {
        lastDamagedAt.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean wasRecentlyDamaged(Player player) {
        Long last = lastDamagedAt.get(player.getUniqueId());
        return last != null && System.currentTimeMillis() - last <= WINDOW_MS;
    }
}
