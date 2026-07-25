package com.nexusuniverse.enchants.trident;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Same snapshot-onto-the-projectile pattern as arrows (see
 * ArrowEnchantListener) for the thrown-trident enchants (Maelstrom,
 * Tempest Call, Tidecaller, Sea Hunter) -- the shooter could swap items
 * before the trident lands. Undertow and the melee half of Sea Hunter
 * are different: they're melee trident hits, not thrown ones, so
 * they're read directly off the wielder's held item, the same way
 * normal weapon enchants work.
 */
public class TridentEnchantListener implements Listener {

    private static final String[] THROWN_TRIDENT_ENCHANT_IDS = {"maelstrom", "tempest_call", "tidecaller", "sea_hunter"};
    private static final long TIDECALLER_REVERT_TICKS = 100; // 5 seconds
    private static final long RIPTIDE_ECHO_WINDOW_MS = 3_000;

    private static final Set<EntityType> AQUATIC = EnumSet.of(
            EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.DROWNED,
            EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH,
            EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.DOLPHIN, EntityType.TURTLE, EntityType.AXOLOTL
    );

    private final Plugin plugin;
    private final EnchantRegistry registry;
    private final Map<UUID, Long> lastRiptide = new HashMap<>();

    public TridentEnchantListener(Plugin plugin, EnchantRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @EventHandler
    public void onRiptide(PlayerRiptideEvent event) {
        Player player = event.getPlayer();
        if (registry.getLevel(event.getItem(), "riptide_echo") > 0) {
            lastRiptide.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;

        Long launchedAt = lastRiptide.get(player.getUniqueId());
        if (launchedAt != null && System.currentTimeMillis() - launchedAt <= RIPTIDE_ECHO_WINDOW_MS) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        ItemStack item = trident.getItemStack();

        for (String id : THROWN_TRIDENT_ENCHANT_IDS) {
            int level = registry.getLevel(item, id);
            if (level > 0) {
                registry.setLevel(trident, id, level);
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;

        int maelstrom = registry.getLevel(trident, "maelstrom");
        if (maelstrom > 0) {
            for (Entity nearby : trident.getNearbyEntities(3, 3, 3)) {
                if (nearby instanceof LivingEntity le) {
                    Vector direction = le.getLocation().toVector().subtract(trident.getLocation().toVector());
                    if (direction.lengthSquared() > 0.0001) {
                        direction.normalize();
                        Vector burst = le.getVelocity().add(direction.multiply(0.5 * maelstrom));
                        burst.setY(Math.max(burst.getY(), 0.3));
                        le.setVelocity(burst);
                    }
                }
            }
        }

        // Deliberately different trigger condition from vanilla Channeling
        // (which needs the player to be standing in rain with sky visible) --
        // this just needs an active thunderstorm in the world, no positioning
        // requirement on the thrower. A related idea, not a copy of the
        // vanilla mechanic's exact rule.
        int tempestCall = registry.getLevel(trident, "tempest_call");
        if (tempestCall > 0 && trident.getWorld().hasStorm()) {
            trident.getWorld().strikeLightning(trident.getLocation());
        }

        int tidecaller = registry.getLevel(trident, "tidecaller");
        if (tidecaller > 0 && event.getHitBlock() != null) {
            placeTemporaryWater(event.getHitBlock());
        }
    }

    /** Remembers the original block so it can put back exactly what was there, not just assume air. */
    private void placeTemporaryWater(Block block) {
        if (block.getType() == Material.WATER || block.getType() == Material.LAVA) return;
        Material original = block.getType();
        block.setType(Material.WATER, false);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (block.getType() == Material.WATER) {
                block.setType(original, false);
            }
        }, TIDECALLER_REVERT_TICKS);
    }

    @EventHandler
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        int undertow = registry.getLevel(attacker.getInventory().getItemInMainHand(), "undertow");
        if (undertow > 0) {
            Vector pull = attacker.getLocation().toVector().subtract(victim.getLocation().toVector());
            if (pull.lengthSquared() > 0.0001) {
                pull.normalize();
                victim.setVelocity(victim.getVelocity().add(pull.multiply(0.3 * undertow)));
            }
        }

        int seaHunterMelee = registry.getLevel(attacker.getInventory().getItemInMainHand(), "sea_hunter");
        if (seaHunterMelee > 0 && AQUATIC.contains(victim.getType())) {
            event.setDamage(event.getDamage() * (1.0 + 0.20 * seaHunterMelee));
        }
    }

    @EventHandler
    public void onTridentDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Trident trident)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        int depthCharge = registry.getLevel(trident, "depth_charge");
        if (depthCharge > 0 && victim.isInWater()) {
            event.setDamage(event.getDamage() * (1.0 + 0.20 * depthCharge));
        }

        int seaHunterThrown = registry.getLevel(trident, "sea_hunter");
        if (seaHunterThrown > 0 && AQUATIC.contains(victim.getType())) {
            event.setDamage(event.getDamage() * (1.0 + 0.20 * seaHunterThrown));
        }
    }
}
