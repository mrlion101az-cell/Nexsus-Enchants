package com.nexusuniverse.enchants.combat;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class WeaponEnchantListener implements Listener {

    private static final Set<org.bukkit.entity.EntityType> UNDEAD = EnumSet.of(
            org.bukkit.entity.EntityType.ZOMBIE, org.bukkit.entity.EntityType.ZOMBIE_VILLAGER,
            org.bukkit.entity.EntityType.HUSK, org.bukkit.entity.EntityType.DROWNED,
            org.bukkit.entity.EntityType.SKELETON, org.bukkit.entity.EntityType.STRAY,
            org.bukkit.entity.EntityType.WITHER_SKELETON, org.bukkit.entity.EntityType.PHANTOM,
            org.bukkit.entity.EntityType.ZOMBIFIED_PIGLIN
    );

    private final EnchantRegistry registry;
    private final BleedManager bleedManager;
    private final RampageManager rampageManager;
    private final DamageTracker damageTracker;
    private final OpeningStrikeTracker openingStrikeTracker;
    private final Random random = new Random();

    // Chain Lightning calls LivingEntity#damage() on secondary targets, which
    // re-fires this same event handler for each one. Without this guard, a
    // chained hit could itself try to chain again -- and could arc right
    // back to a target already hit, since the immediate-neighbor exclusion
    // only knows about that one hop. This flag stops the cascade at one
    // level deep: the original hit arcs out, but none of the arced hits
    // arc further. Everything else (vampiric, venom, etc.) still applies
    // normally to each chained hit -- only chain-lightning-triggering-
    // chain-lightning is blocked.
    private boolean processingChainLightning = false;

    public WeaponEnchantListener(EnchantRegistry registry, BleedManager bleedManager, RampageManager rampageManager,
                                  DamageTracker damageTracker, OpeningStrikeTracker openingStrikeTracker) {
        this.registry = registry;
        this.bleedManager = bleedManager;
        this.rampageManager = rampageManager;
        this.damageTracker = damageTracker;
        this.openingStrikeTracker = openingStrikeTracker;
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        int rampage = registry.getLevel(killer.getInventory().getItemInMainHand(), "rampage");
        if (rampage > 0) {
            rampageManager.onKill(killer, rampage * 3);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        ItemStack weapon = attacker.getInventory().getItemInMainHand();

        int vampiric = registry.getLevel(weapon, "vampiric");
        if (vampiric > 0) {
            double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
            double heal = event.getDamage() * 0.10 * vampiric;
            attacker.setHealth(Math.min(maxHealth, attacker.getHealth() + heal));
        }

        int inferno = registry.getLevel(weapon, "inferno");
        if (inferno > 0) {
            victim.setFireTicks(Math.max(victim.getFireTicks(), 60 * inferno));
        }

        int frostbite = registry.getLevel(weapon, "frostbite");
        if (frostbite > 0 && random.nextDouble() < 0.25 * frostbite) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, frostbite - 1));
        }

        int venom = registry.getLevel(weapon, "venom");
        if (venom > 0 && random.nextDouble() < 0.25 * venom) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, venom - 1));
        }

        int executioner = registry.getLevel(weapon, "executioner");
        if (executioner > 0) {
            double maxHealth = victim.getAttribute(Attribute.MAX_HEALTH).getValue();
            if (victim.getHealth() / maxHealth <= 0.20) {
                event.setDamage(event.getDamage() * (1.0 + 0.25 * executioner));
            }
        }

        int bleed = registry.getLevel(weapon, "bleed");
        if (bleed > 0) {
            bleedManager.applyBleed(victim, bleed);
        }

        int soulReaper = registry.getLevel(weapon, "soul_reaper");
        if (soulReaper > 0 && UNDEAD.contains(victim.getType())) {
            event.setDamage(event.getDamage() + 2.0 * soulReaper);
            double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
            attacker.setHealth(Math.min(maxHealth, attacker.getHealth() + 1.0));
        }

        int stun = registry.getLevel(weapon, "stun");
        if (stun > 0 && random.nextDouble() < 0.15 * stun) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 9));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 9));
        }

        int knockbackWave = registry.getLevel(weapon, "knockback_wave");
        if (knockbackWave > 0) {
            for (Entity nearby : victim.getNearbyEntities(3, 3, 3)) {
                if (nearby instanceof LivingEntity le && !nearby.equals(attacker)) {
                    Vector direction = le.getLocation().toVector().subtract(attacker.getLocation().toVector());
                    if (direction.lengthSquared() > 0.0001) {
                        direction.normalize();
                        le.setVelocity(le.getVelocity().add(direction.multiply(0.4 * knockbackWave)));
                    }
                }
            }
        }

        int thunderstrike = registry.getLevel(weapon, "thunderstrike");
        if (thunderstrike > 0 && random.nextDouble() < 0.10 * thunderstrike) {
            victim.getWorld().strikeLightningEffect(victim.getLocation()); // visual + sound only, no extra fire/damage
        }

        int rampage = registry.getLevel(weapon, "rampage");
        if (rampage > 0) {
            int stacks = rampageManager.getStacks(attacker);
            if (stacks > 0) {
                event.setDamage(event.getDamage() * (1.0 + 0.05 * stacks));
            }
        }

        int flurry = registry.getLevel(weapon, "flurry");
        if (flurry > 0 && random.nextDouble() < 0.20 * flurry) {
            // Direct health reduction, not a second damage() call -- avoids
            // re-triggering this whole handler a second time for one swing
            // (which would double-apply vampiric/venom/bleed/etc. on top of
            // an already-bonus hit). The bonus hit is real damage, just not
            // an event other enchants get to react to a second time.
            double bonus = event.getDamage() * 0.5;
            victim.setHealth(Math.max(0, victim.getHealth() - bonus));
        }

        if (!processingChainLightning) {
            int chainLightning = registry.getLevel(weapon, "chain_lightning");
            if (chainLightning > 0) {
                processingChainLightning = true;
                try {
                    double bonus = event.getDamage() * 0.3;
                    int hitCount = 0;
                    for (Entity nearby : victim.getNearbyEntities(4, 4, 4)) {
                        if (hitCount >= chainLightning) break;
                        if (nearby instanceof LivingEntity le && !nearby.equals(attacker) && !nearby.equals(victim)) {
                            le.damage(bonus, attacker);
                            hitCount++;
                        }
                    }
                } finally {
                    processingChainLightning = false;
                }
            }
        }

        int curseOfMediocrity = registry.getLevel(weapon, "curse_of_dullness");
        if (curseOfMediocrity > 0) {
            event.setDamage(event.getDamage() * (1.0 - 0.10 * curseOfMediocrity));
        }

        int momentumStrike = registry.getLevel(weapon, "momentum_strike");
        if (momentumStrike > 0) {
            double speed = attacker.getVelocity().length();
            double bonus = Math.min(2.0, speed * 4.0) * momentumStrike;
            event.setDamage(event.getDamage() + bonus);
        }

        int lastStand = registry.getLevel(weapon, "last_stand");
        if (lastStand > 0) {
            double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
            if (attacker.getHealth() / maxHealth <= 0.30) {
                event.setDamage(event.getDamage() * (1.0 + 0.20 * lastStand));
            }
        }

        int adrenaline = registry.getLevel(weapon, "adrenaline");
        if (adrenaline > 0 && damageTracker.wasRecentlyDamaged(attacker)) {
            event.setDamage(event.getDamage() * (1.0 + 0.15 * adrenaline));
        }

        int armorBreaker = registry.getLevel(weapon, "armor_breaker");
        if (armorBreaker > 0) {
            var equipment = victim.getEquipment();
            if (equipment != null) {
                long armorPieces = java.util.stream.Stream.of(
                                equipment.getHelmet(), equipment.getChestplate(),
                                equipment.getLeggings(), equipment.getBoots())
                        .filter(item -> item != null && item.getType() != org.bukkit.Material.AIR)
                        .count();
                if (armorPieces >= 3) {
                    event.setDamage(event.getDamage() + 1.0 * armorBreaker);
                }
            }
        }

        int openingStrike = registry.getLevel(weapon, "opening_strike");
        if (openingStrike > 0 && openingStrikeTracker.isFreshTarget(attacker, victim)) {
            event.setDamage(event.getDamage() * (1.0 + 0.20 * openingStrike));
        }

        int trueEdge = registry.getLevel(weapon, "true_edge");
        if (trueEdge > 0) {
            var armorAttribute = victim.getAttribute(Attribute.ARMOR);
            double armorValue = armorAttribute != null ? armorAttribute.getValue() : 0;
            event.setDamage(event.getDamage() + armorValue * 0.10 * trueEdge);
        }

        int giantSlayer = registry.getLevel(weapon, "giant_slayer");
        if (giantSlayer > 0) {
            var attackerMaxHealth = attacker.getAttribute(Attribute.MAX_HEALTH);
            var victimMaxHealth = victim.getAttribute(Attribute.MAX_HEALTH);
            if (attackerMaxHealth != null && victimMaxHealth != null) {
                double diff = victimMaxHealth.getValue() - attackerMaxHealth.getValue();
                if (diff > 0) {
                    event.setDamage(event.getDamage() + Math.min(6.0, diff * 0.05) * giantSlayer);
                }
            }
        }

        int momentumBreaker = registry.getLevel(weapon, "momentum_breaker");
        if (momentumBreaker > 0 && !victim.isOnGround()) {
            event.setDamage(event.getDamage() * (1.0 + 0.15 * momentumBreaker));
        }

        int vitalStrike = registry.getLevel(weapon, "vital_strike");
        if (vitalStrike > 0 && random.nextDouble() < 0.20 * vitalStrike) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, vitalStrike - 1));
            event.setDamage(event.getDamage() * 1.10);
        }

        int ambush = registry.getLevel(weapon, "ambush");
        if (ambush > 0 && victim instanceof org.bukkit.entity.Mob mob && mob.getTarget() == null) {
            event.setDamage(event.getDamage() * (1.0 + 0.25 * ambush));
        }
    }
}
