package com.nexusuniverse.enchants.lead;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Leashing consumes the lead item into an invisible connection -- once
 * an entity is leashed, there's no ItemStack left to read enchant tags
 * off of. So the level gets captured into a small map at the moment of
 * leashing (onLeash) and read back from that map during the periodic
 * tick, keyed by the leashed entity's UUID, cleared on unleash. Calming
 * Lead is the one exception: it only needs to fire once, at the moment
 * of leashing, so it doesn't need this map at all.
 */
public class LeadEnchantListener implements Listener {

    private final EnchantRegistry registry;
    private final Random random = new Random();
    private final Map<UUID, Integer> swiftLeadLevels = new HashMap<>();
    private final Map<UUID, Integer> frayedCurseLevels = new HashMap<>();

    public LeadEnchantListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onLeash(PlayerLeashEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack lead = leadInHand(player);
        if (lead == null) return;

        UUID entityId = event.getEntity().getUniqueId();

        int calming = registry.getLevel(lead, "calming_lead");
        if (calming > 0 && event.getEntity() instanceof Mob mob) {
            mob.setTarget(null);
        }

        int swiftLead = registry.getLevel(lead, "swift_lead");
        if (swiftLead > 0) {
            swiftLeadLevels.put(entityId, swiftLead);
        }

        int frayedCurse = registry.getLevel(lead, "curse_of_the_frayed_lead");
        if (frayedCurse > 0) {
            frayedCurseLevels.put(entityId, frayedCurse);
        }
    }

    @EventHandler
    public void onUnleash(PlayerUnleashEntityEvent event) {
        UUID entityId = event.getEntity().getUniqueId();
        swiftLeadLevels.remove(entityId);
        frayedCurseLevels.remove(entityId);
    }

    /** Called once per second from the central tick loop, for every online player. */
    public void tick(Player player) {
        if (swiftLeadLevels.isEmpty() && frayedCurseLevels.isEmpty()) return;

        for (Entity nearby : player.getNearbyEntities(12, 12, 12)) {
            if (!nearby.isLeashed() || !(nearby instanceof LivingEntity leashed)) continue;
            if (!(leashed.getLeashHolder() instanceof Player holder) || !holder.equals(player)) continue;

            Integer swift = swiftLeadLevels.get(leashed.getUniqueId());
            if (swift != null && swift > 0) {
                leashed.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, swift - 1, true, false));
            }

            Integer curse = frayedCurseLevels.get(leashed.getUniqueId());
            if (curse != null && curse > 0 && random.nextDouble() < 0.05 * curse) {
                leashed.setLeashHolder(null);
                swiftLeadLevels.remove(leashed.getUniqueId());
                frayedCurseLevels.remove(leashed.getUniqueId());
                player.sendMessage("§4The frayed lead snaps -- it slips free.");
            }
        }
    }

    private ItemStack leadInHand(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.getType() == Material.LEAD) return main;
        ItemStack off = player.getInventory().getItemInOffHand();
        if (off.getType() == Material.LEAD) return off;
        return null;
    }
}
