package com.nexusuniverse.enchants.pumpkin;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks whether a player has stood roughly still for at least 3
 * seconds, for Scarecrow. Updated once per second from the central
 * tick loop -- so "3 seconds" here really means "3 consecutive
 * once-per-second checks all landed within the movement tolerance,"
 * which is close enough for this purpose but not frame-perfect.
 */
public class StillnessTracker {

    private static final double MOVE_TOLERANCE = 0.3;
    private static final int STILL_SECONDS_REQUIRED = 3;

    private final Map<UUID, Location> lastPosition = new HashMap<>();
    private final Map<UUID, Integer> stillSeconds = new HashMap<>();

    /** Called once per second from the central tick loop. */
    public void tick(Player player) {
        UUID id = player.getUniqueId();
        Location current = player.getLocation();
        Location last = lastPosition.get(id);

        if (last != null && last.getWorld().equals(current.getWorld()) && last.distance(current) < MOVE_TOLERANCE) {
            stillSeconds.merge(id, 1, Integer::sum);
        } else {
            stillSeconds.put(id, 0);
        }
        lastPosition.put(id, current);
    }

    public boolean hasBeenStill(Player player) {
        return stillSeconds.getOrDefault(player.getUniqueId(), 0) >= STILL_SECONDS_REQUIRED;
    }
}
