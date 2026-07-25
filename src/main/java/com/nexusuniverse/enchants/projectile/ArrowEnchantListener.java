package com.nexusuniverse.enchants.projectile;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Bow enchants work differently from melee weapon enchants: by the time
 * an arrow lands, the shooter may have swapped items, so we can't just
 * check "what's in their hand" at hit time. Instead, the enchant levels
 * get snapshotted onto the arrow entity's own PersistentDataContainer
 * the moment it's fired (onShoot), then read back off the arrow itself
 * at impact (onHit / onDamage) -- the arrow carries its own enchant
 * state independent of whatever the shooter is holding by then.
 */
public class ArrowEnchantListener implements Listener {

    private static final String[] BOW_ENCHANT_IDS = {
            "explosive_arrows", "poisoned_arrows", "vampiric_arrows",
            "confusing_arrows", "blink_shot", "longshot", "ricochet", "piercing_bolt", "close_quarters"
    };

    private final EnchantRegistry registry;
    private final Map<UUID, Location> shootOrigin = new HashMap<>();
    private final Random random = new Random();

    public ArrowEnchantListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof Arrow arrow)) return;
        ItemStack bow = event.getBow();
        if (bow == null) return;

        for (String id : BOW_ENCHANT_IDS) {
            int level = registry.getLevel(bow, id);
            if (level > 0) {
                registry.setLevel(arrow, id, level);
            }
        }
        shootOrigin.put(arrow.getUniqueId(), arrow.getLocation());

        int windShot = registry.getLevel(bow, "wind_shot");
        if (windShot > 0) {
            arrow.setVelocity(arrow.getVelocity().multiply(1.0 + 0.15 * windShot));
        }

        int doubleTap = registry.getLevel(bow, "double_tap");
        if (doubleTap > 0 && random.nextDouble() < 0.20 * doubleTap
                && event.getEntity() instanceof Player shooter) {
            shooter.getInventory().addItem(new ItemStack(org.bukkit.Material.ARROW));
        }

        int eagleEye = registry.getLevel(bow, "eagle_eye");
        if (eagleEye > 0) {
            arrow.setVelocity(arrow.getVelocity().add(new org.bukkit.util.Vector(0, 0.02 * eagleEye, 0)));
        }

        int focusShot = registry.getLevel(bow, "focus_shot");
        if (focusShot > 0) {
            arrow.setDamage(arrow.getDamage() + event.getForce() * 1.5 * focusShot);
        }

        int twinBolt = registry.getLevel(bow, "twin_bolt");
        if (twinBolt > 0 && random.nextDouble() < 0.20 * twinBolt && event.getEntity() instanceof Player twinShooter) {
            Arrow extra = twinShooter.launchProjectile(Arrow.class);
            extra.setVelocity(arrow.getVelocity());
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;

        int explosive = registry.getLevel(arrow, "explosive_arrows");
        if (explosive > 0) {
            Entity source = arrow.getShooter() instanceof Entity e ? e : null;
            arrow.getWorld().createExplosion(arrow.getLocation(), 1.5f * explosive, false, false, source);
        }

        int enderBow = registry.getLevel(arrow, "blink_shot");
        if (enderBow > 0 && arrow.getShooter() instanceof Player shooter) {
            shooter.teleport(arrow.getLocation());
        }

        shootOrigin.remove(arrow.getUniqueId());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        int poisoned = registry.getLevel(arrow, "poisoned_arrows");
        if (poisoned > 0) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, poisoned - 1));
        }

        int confusing = registry.getLevel(arrow, "confusing_arrows");
        if (confusing > 0) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, confusing - 1));
        }

        int vampiric = registry.getLevel(arrow, "vampiric_arrows");
        if (vampiric > 0 && arrow.getShooter() instanceof Player shooter) {
            double maxHealth = shooter.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            shooter.setHealth(Math.min(maxHealth, shooter.getHealth() + event.getDamage() * 0.15 * vampiric));
        }

        int sniper = registry.getLevel(arrow, "longshot");
        if (sniper > 0) {
            Location origin = shootOrigin.get(arrow.getUniqueId());
            if (origin != null) {
                double distanceTraveled = origin.distance(arrow.getLocation());
                double bonus = Math.min(2.0, distanceTraveled / 20.0) * sniper;
                event.setDamage(event.getDamage() + bonus);
            }
        }

        // Ricochet doesn't literally redirect the physical arrow (it's
        // already consumed by the hit) -- it applies a reduced-damage hit
        // to one other nearby target instead, which is the honest
        // equivalent of "the shot found a second target" without pretending
        // the same arrow entity bounced through the air.
        int ricochet = registry.getLevel(arrow, "ricochet");
        if (ricochet > 0 && random.nextDouble() < 0.30 * ricochet) {
            Entity shooterEntity = arrow.getShooter() instanceof Entity e ? e : null;
            for (Entity nearby : victim.getNearbyEntities(4, 4, 4)) {
                if (nearby instanceof LivingEntity le && !nearby.equals(victim) && !nearby.equals(shooterEntity)) {
                    le.damage(event.getDamage() * 0.5, shooterEntity);
                    break;
                }
            }
        }

        int piercingBolt = registry.getLevel(arrow, "piercing_bolt");
        if (piercingBolt > 0 && arrow.getPierceLevel() > 0) {
            event.setDamage(event.getDamage() + arrow.getPierceLevel() * 0.5 * piercingBolt);
        }

        int closeQuarters = registry.getLevel(arrow, "close_quarters");
        if (closeQuarters > 0) {
            Location origin = shootOrigin.get(arrow.getUniqueId());
            if (origin != null && origin.distance(arrow.getLocation()) < 5.0) {
                event.setDamage(event.getDamage() * (1.0 + 0.25 * closeQuarters));
            }
        }
    }

    @EventHandler
    public void onMobDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();

        int volleyCall = registry.getLevel(weapon, "volley_call");
        if (volleyCall > 0 && random.nextDouble() < 0.30 * volleyCall) {
            killer.getInventory().addItem(new ItemStack(org.bukkit.Material.ARROW));
        }

        int boltStorm = registry.getLevel(weapon, "bolt_storm");
        if (boltStorm > 0) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, boltStorm - 1));
        }
    }
}
