package com.nexusuniverse.enchants.mace;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Vanilla's own mace "smash attack" bonus (and its dedicated Density/
 * Wind Burst/Breach enchants) triggers based on how far the player fell
 * before the hit, with roughly a 1.5-block threshold. Bukkit doesn't
 * expose a direct "was this a smash" flag on the damage event, so this
 * uses the same fall-distance check vanilla itself uses
 * (LivingEntity#getFallDistance() >= 1.5 at the moment of the hit) as a
 * reasonable, honest proxy -- not a guess at an unrelated number.
 *
 * These enchants deliberately don't duplicate what Density/Wind Burst/
 * Breach already do (raw smash damage scaling, armor-piercing knockback,
 * breaking blocks) -- they're additional effects layered on a smash hit,
 * not replacements for the vanilla mace enchants.
 */
public class MaceEnchantListener implements Listener {

    private static final double SMASH_FALL_THRESHOLD = 1.5;

    private final EnchantRegistry registry;
    private final Random random = new Random();

    public MaceEnchantListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon.getType() != Material.MACE) return;

        int relentless = registry.getLevel(weapon, "relentless");
        double threshold = relentless > 0 ? Math.max(0.3, SMASH_FALL_THRESHOLD - 0.3 * relentless) : SMASH_FALL_THRESHOLD;
        boolean isSmash = attacker.getFallDistance() >= threshold;

        if (isSmash) {
            int seismicSlam = registry.getLevel(weapon, "seismic_slam");
            if (seismicSlam > 0) {
                for (Entity nearby : victim.getNearbyEntities(3, 3, 3)) {
                    if (nearby instanceof LivingEntity le && !nearby.equals(attacker)) {
                        Vector direction = le.getLocation().toVector().subtract(victim.getLocation().toVector());
                        if (direction.lengthSquared() < 0.0001) continue;
                        direction.normalize();
                        le.setVelocity(le.getVelocity().add(direction.multiply(0.5 * seismicSlam)));
                    }
                }
            }

            int aftershock = registry.getLevel(weapon, "aftershock");
            if (aftershock > 0) {
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOWNESS, 60, aftershock - 1));
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.MINING_FATIGUE, 60, aftershock - 1));
            }

            int windfall = registry.getLevel(weapon, "windfall");
            if (windfall > 0 && random.nextDouble() < 0.30 * windfall) {
                Vector velocity = victim.getVelocity();
                victim.setVelocity(new Vector(velocity.getX(), 0.8, velocity.getZ()));
            }

            int warlordsFury = registry.getLevel(weapon, "warlords_fury");
            if (warlordsFury > 0) {
                attacker.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.STRENGTH, 60, warlordsFury - 1));
            }

            int sunderingSmash = registry.getLevel(weapon, "sundering_smash");
            if (sunderingSmash > 0) {
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.WEAKNESS, 100, sunderingSmash - 1));
            }

            int impactTremor = registry.getLevel(weapon, "impact_tremor");
            if (impactTremor > 0) {
                for (Entity nearby : victim.getNearbyEntities(8, 8, 8)) {
                    if (nearby instanceof org.bukkit.entity.Monster monster) {
                        monster.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.GLOWING, 100, 0, true, false));
                    }
                }
            }
        } else {
            int heavyHands = registry.getLevel(weapon, "curse_of_heavy_hands");
            if (heavyHands > 0) {
                event.setDamage(event.getDamage() * (1.0 - 0.15 * heavyHands));
            }

            int concussiveBlow = registry.getLevel(weapon, "concussive_blow");
            if (concussiveBlow > 0) {
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOWNESS, 40, concussiveBlow - 1));
            }
        }
    }
}
