package com.nexusuniverse.enchants.horse;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Horse armor is read via AbstractHorse#getInventory().getArmor() --
 * that method only exists on the HorseInventory subtype (Donkeys,
 * Mules, and Llamas use differently-shaped inventories without an
 * armor slot), so every read here first narrows to HorseInventory
 * and quietly skips anything that isn't one. The ridden-horse tick (swift_gallop,
 * regal_bearing) is called once per second from the plugin's central
 * loop, iterating every loaded world's horses and filtering to ones
 * with a passenger -- on a server with a lot of loaded-but-unridden
 * horses (breeding farms, etc.) this does mean checking all of them
 * every second just to filter most back out, which is worth knowing
 * about if you run something horse-heavy.
 */
public class HorseArmorListener implements Listener {

    private final EnchantRegistry registry;

    public HorseArmorListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse horse)) return;
        if (!(horse.getInventory() instanceof HorseInventory horseInventory)) return;
        ItemStack armor = horseInventory.getArmor();
        if (armor == null) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (registry.getLevel(armor, "steady_gait") > 0) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.CONTACT) {
            if (registry.getLevel(armor, "sure_hooves") > 0) {
                event.setCancelled(true);
                return;
            }
        }

        int warCharger = registry.getLevel(armor, "war_charger");
        if (warCharger > 0) {
            event.setDamage(event.getDamage() * (1.0 - 0.10 * warCharger));
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            int beastOfBurden = registry.getLevel(armor, "beast_of_burden");
            if (beastOfBurden > 0) {
                event.setDamage(event.getDamage() * (1.0 - 0.20 * beastOfBurden));
            }
        }

        int packCurse = registry.getLevel(armor, "curse_of_the_pack");
        if (packCurse > 0) {
            event.setDamage(event.getDamage() * (1.0 + 0.15 * packCurse));
        }
    }

    /** Called once per second from the central tick loop, once per loaded ridden horse. */
    public void tickRiddenHorse(AbstractHorse horse) {
        if (!(horse.getInventory() instanceof HorseInventory horseInventory)) return;
        ItemStack armor = horseInventory.getArmor();
        if (armor == null) return;

        int swiftGallop = registry.getLevel(armor, "swift_gallop");
        if (swiftGallop > 0) {
            horse.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, swiftGallop - 1, true, false));
        }

        int regalBearing = registry.getLevel(armor, "regal_bearing");
        if (regalBearing > 0) {
            horse.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, regalBearing - 1, true, false));
        }

        int ironShoes = registry.getLevel(armor, "iron_shoes");
        if (ironShoes > 0) {
            horse.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, true, false));
        }

        int nimbleSteed = registry.getLevel(armor, "nimble_steed");
        if (nimbleSteed > 0) {
            horse.setJumpStrength(Math.min(2.0, horse.getJumpStrength() + 0.1 * nimbleSteed));
        }
    }
}
