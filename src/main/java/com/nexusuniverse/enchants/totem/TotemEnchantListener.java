package com.nexusuniverse.enchants.totem;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Vanilla's totem save already grants its own Regeneration, Absorption,
 * and negative-effect clearing before this event finishes resolving --
 * these enchants layer additional effects on top a tick later, rather
 * than trying to replace what vanilla already does.
 *
 * Curse of the Lost is checked first and can cancel the save outright --
 * a real risk/reward curse, not just a flavor debuff.
 */
public class TotemEnchantListener implements Listener {

    private final Plugin plugin;
    private final EnchantRegistry registry;
    private final Random random = new Random();
    private final Map<UUID, Long> wardUntil = new HashMap<>();

    public TotemEnchantListener(Plugin plugin, EnchantRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @EventHandler
    public void onResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.isCancelled()) return;

        EquipmentSlot hand = event.getHand();
        ItemStack totem = player.getInventory().getItem(hand);
        if (totem == null || totem.getType() != Material.TOTEM_OF_UNDYING) return;

        int lostCurse = registry.getLevel(totem, "curse_of_the_lost");
        if (lostCurse > 0 && random.nextDouble() < 0.10 * lostCurse) {
            event.setCancelled(true);
            return;
        }

        int lifeline = registry.getLevel(totem, "lifeline");
        int echoing = registry.getLevel(totem, "echoing_totem");
        int guardianSpirit = registry.getLevel(totem, "guardian_spirit");
        int finalGift = registry.getLevel(totem, "final_gift");
        int totemicWard = registry.getLevel(totem, "totemic_ward");
        int vengefulSpirit = registry.getLevel(totem, "vengeful_spirit");
        if (lifeline == 0 && echoing == 0 && guardianSpirit == 0 && finalGift == 0
                && totemicWard == 0 && vengefulSpirit == 0) return;

        if (totemicWard > 0) {
            wardUntil.put(player.getUniqueId(), System.currentTimeMillis() + 1000L * totemicWard);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (lifeline > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200 + 100 * lifeline, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200 + 100 * lifeline, 1, true, false));
            }
            if (guardianSpirit > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0, true, false));
            }
            if (finalGift > 0) {
                player.setFoodLevel(20);
                player.setSaturation(5.0f);
            }
            if (vengefulSpirit > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0, true, false));
            }
            if (echoing > 0 && random.nextDouble() < 0.15 * echoing) {
                ItemStack current = player.getInventory().getItem(hand);
                if (current == null || current.getType() == Material.AIR) {
                    player.getInventory().setItem(hand, new ItemStack(Material.TOTEM_OF_UNDYING));
                    player.sendMessage("§dYour totem echoes back into existence.");
                }
            }
        }, 1L);
    }

    @EventHandler
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Long until = wardUntil.get(player.getUniqueId());
        if (until != null && System.currentTimeMillis() < until) {
            event.setCancelled(true);
        }
    }
}
