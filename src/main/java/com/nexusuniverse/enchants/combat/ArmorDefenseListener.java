package com.nexusuniverse.enchants.combat;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import io.papermc.paper.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class ArmorDefenseListener implements Listener {

    private final EnchantRegistry registry;
    private final SecondWindManager secondWindManager;
    private final DamageTracker damageTracker;
    private final Random random = new Random();

    public ArmorDefenseListener(EnchantRegistry registry, SecondWindManager secondWindManager, DamageTracker damageTracker) {
        this.registry = registry;
        this.secondWindManager = secondWindManager;
        this.damageTracker = damageTracker;
    }

    /** Thorns+ needs the attacker, so it's handled separately from the generic-cause effects below. */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;

        int thorns = registry.maxArmorLevel(victim, "thorns_plus");
        if (thorns > 0) {
            attacker.damage(event.getDamage() * 0.10 * thorns);
        }

        if (attacker instanceof org.bukkit.entity.Monster) {
            int monsterWard = registry.maxArmorLevel(victim, "monster_ward");
            if (monsterWard > 0) {
                event.setDamage(event.getDamage() * (1.0 - 0.10 * monsterWard));
            }
        }

        int highGround = registry.maxArmorLevel(victim, "high_ground");
        if (highGround > 0 && attacker.getLocation().getY() < victim.getLocation().getY() - 1.0) {
            event.setDamage(event.getDamage() * (1.0 - 0.08 * highGround));
        }
    }

    /** Reduces (not eliminates) knockback taken, scaled by level. Paper-specific event -- lets us scale the actual applied knockback vector directly instead of fighting velocity after the fact. */
    @EventHandler
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        int steadyFooting = registry.maxArmorLevel(victim, "steady_footing");
        if (steadyFooting > 0) {
            double reduction = Math.min(0.9, 0.2 * steadyFooting);
            event.setKnockback(event.getKnockback().multiply(1.0 - reduction));
        }

        int steadyScope = registry.getLevel(victim.getInventory().getItemInMainHand(), "steady_scope");
        if (steadyScope > 0) {
            double reduction = Math.min(0.9, 0.15 * steadyScope);
            event.setKnockback(event.getKnockback().multiply(1.0 - reduction));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        damageTracker.recordDamage(victim);

        if (event.getCause() == EntityDamageEvent.DamageCause.FREEZE) {
            if (registry.maxArmorLevel(victim, "insulation") > 0) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (registry.maxArmorLevel(victim, "featherfall_plus") > 0) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            int bulwark = registry.maxArmorLevel(victim, "bulwark");
            if (bulwark > 0 && random.nextDouble() < 0.08 * bulwark) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            int guardian = registry.maxArmorLevel(victim, "guardian");
            if (guardian > 0) {
                event.setDamage(event.getDamage() * (1.0 - 0.15 * guardian));
            }
        }

        int ironhide = registry.maxArmorLevel(victim, "ironhide");
        if (ironhide > 0) {
            event.setDamage(event.getDamage() * (1.0 - 0.05 * ironhide));
        }

        int curseOfBreaking = registry.maxArmorLevel(victim, "curse_of_vulnerability");
        if (curseOfBreaking > 0) {
            event.setDamage(event.getDamage() * (1.0 + 0.10 * curseOfBreaking));
        }

        // Second Wind: if this hit would be lethal, and it's off cooldown, survive it instead.
        if (event.getFinalDamage() >= victim.getHealth()
                && registry.maxArmorLevel(victim, "second_wind") > 0
                && secondWindManager.tryTrigger(victim)) {
            event.setCancelled(true);
            double maxHealth = victim.getAttribute(Attribute.MAX_HEALTH).getValue();
            victim.setHealth(Math.max(1.0, maxHealth * 0.30));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
        }
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        int wisdom = registry.maxArmorLevel(player, "wisdom");
        if (wisdom > 0) {
            event.setAmount((int) Math.round(event.getAmount() * (1.0 + 0.15 * wisdom)));
        }
    }
}
