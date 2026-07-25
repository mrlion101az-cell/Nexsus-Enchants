package com.nexusuniverse.enchants.firework;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * All four only trigger when the firework is used while actually
 * gliding (Player#isGliding()) -- that's the same real condition
 * vanilla's own elytra-boost mechanic requires, so this doesn't fire on
 * every firework use, just the elytra-boost ones.
 *
 * Stacked fireworks sharing an enchant is a new wrinkle for this
 * plugin -- everything before this was a single, non-stackable gear
 * item. A tagged stack behaves the same as any tagged item as far as
 * this system is concerned (the whole stack shares one PDC), which is
 * correct and expected, just a first for this plugin.
 */
public class FireworkEnchantListener implements Listener {

    private final Plugin plugin;
    private final EnchantRegistry registry;
    private final java.util.Random random = new java.util.Random();

    public FireworkEnchantListener(Plugin plugin, EnchantRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!player.isGliding()) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FIREWORK_ROCKET) return;

        int thruster = registry.getLevel(item, "thruster");
        int safeBurst = registry.getLevel(item, "safe_burst");
        int showstopper = registry.getLevel(item, "showstopper");
        int dudCurse = registry.getLevel(item, "curse_of_the_dud");
        int encore = registry.getLevel(item, "encore");
        if (thruster == 0 && safeBurst == 0 && showstopper == 0 && dudCurse == 0 && encore == 0) return;

        if (thruster > 0) {
            Vector boost = player.getLocation().getDirection().normalize().multiply(0.15 * thruster);
            player.setVelocity(player.getVelocity().add(boost));
        }
        if (safeBurst > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, true, false));
        }
        if (showstopper > 0) {
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation(), 20 * showstopper, 0.3, 0.3, 0.3, 0.05);
        }
        if (dudCurse > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Vector velocity = player.getVelocity();
                player.setVelocity(new Vector(velocity.getX(), velocity.getY() * (1.0 - 0.15 * dudCurse), velocity.getZ()));
            }, 2L);
        }
        if (encore > 0 && random.nextDouble() < 0.20 * encore) {
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    player.getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET)), 1L);
        }
    }
}
