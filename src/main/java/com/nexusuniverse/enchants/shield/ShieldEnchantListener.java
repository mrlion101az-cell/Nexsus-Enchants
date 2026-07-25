package com.nexusuniverse.enchants.shield;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * All four shield enchants only do anything while the player is
 * actively blocking (Player#isBlocking()) with a shield carrying the
 * relevant tag, in either hand. Vanilla's own blocking damage reduction
 * happens as part of core game logic ahead of this listener, so
 * everything here layers on top of (not instead of) that.
 *
 * Rebound Ward specifically hasn't been verified against a live server:
 * reversing a projectile's velocity the instant it registers a hit on a
 * blocking shield is the reasonable API-level approach, but whether the
 * projectile entity is still in a state where that has a visible effect
 * (versus already being marked stuck/consumed by the time this event
 * fires) is genuinely something only a real test can confirm.
 */
public class ShieldEnchantListener implements Listener {

    private final EnchantRegistry registry;
    private final Random random = new Random();

    public ShieldEnchantListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        // Last Line works even when not actively blocking, so it's checked
        // before the isBlocking() gate that everything else below needs.
        if (!victim.isBlocking()) {
            ItemStack passiveShield = shieldInHand(victim);
            int lastLine = passiveShield != null ? registry.getLevel(passiveShield, "last_line") : 0;
            if (lastLine > 0 && random.nextDouble() < 0.05 * lastLine) {
                event.setCancelled(true);
            }
            return;
        }

        ItemStack shield = shieldInHand(victim);
        if (shield == null) return;

        int rebound = registry.getLevel(shield, "rebound_ward");
        if (rebound > 0 && event.getDamager() instanceof Projectile projectile) {
            projectile.setVelocity(projectile.getVelocity().multiply(-1));
        }

        int vanguard = registry.getLevel(shield, "vanguard");
        if (vanguard > 0 && event.getDamager() instanceof Projectile) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, vanguard - 1));
        }

        int stagger = registry.getLevel(shield, "stagger_guard");
        if (stagger > 0 && event.getDamager() instanceof LivingEntity attacker) {
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, stagger - 1));
        }

        int drainward = registry.getLevel(shield, "drainward");
        if (drainward > 0) {
            double maxHealth = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            victim.setHealth(Math.min(maxHealth, victim.getHealth() + 0.5 * drainward));
        }

        int spikeWall = registry.getLevel(shield, "spike_wall");
        if (spikeWall > 0 && event.getDamager() instanceof LivingEntity attacker) {
            attacker.damage(1.0 * spikeWall);
        }

        int fortify = registry.getLevel(shield, "fortify");
        if (fortify > 0) {
            event.setDamage(event.getDamage() * (1.0 - 0.10 * fortify));
        }

        int aegisWard = registry.getLevel(shield, "aegis_ward");
        if (aegisWard > 0) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, aegisWard - 1));
        }

        int clumsiness = registry.getLevel(shield, "curse_of_clumsiness");
        if (clumsiness > 0 && random.nextDouble() < 0.15 * clumsiness) {
            event.setDamage(event.getDamage() + 2.0 * clumsiness);
        }
    }

    private ItemStack shieldInHand(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.getType() == Material.SHIELD) return main;
        ItemStack off = player.getInventory().getItemInOffHand();
        if (off.getType() == Material.SHIELD) return off;
        return null;
    }

    @EventHandler
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!victim.isBlocking()) return;

        ItemStack shield = shieldInHand(victim);
        if (shield == null) return;

        int unyielding = registry.getLevel(shield, "unyielding");
        if (unyielding > 0) {
            double reduction = Math.min(1.0, 0.3 * unyielding);
            event.setKnockback(event.getKnockback().multiply(1.0 - reduction));
        }
    }
}
