package com.nexusuniverse.enchants.fishing;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class FishingEnchantListener implements Listener {

    private static final java.util.Set<Material> JUNK_MATERIALS = java.util.Set.of(
            Material.LEATHER_BOOTS, Material.LEATHER, Material.BOWL, Material.STICK,
            Material.STRING, Material.ROTTEN_FLESH, Material.BONE, Material.POTION,
            Material.BOW, Material.FISHING_ROD, Material.TRIPWIRE_HOOK, Material.INK_SAC
    );
    private static final java.util.List<Material> FISH_MATERIALS = java.util.List.of(
            Material.COD, Material.SALMON, Material.PUFFERFISH, Material.TROPICAL_FISH
    );

    private final EnchantRegistry registry;
    private final Random random = new Random();
    private final java.util.Map<java.util.UUID, Long> castStartTime = new java.util.HashMap<>();

    public FishingEnchantListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (event.getItem().getType() != Material.FISHING_ROD) return;
        if (registry.getLevel(event.getItem(), "line_saver") > 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();

        if (event.getState() == PlayerFishEvent.State.FISHING) {
            int quickBite = registry.getLevel(rod, "quick_bite");
            if (quickBite > 0 && event.getHook() != null) {
                // Real Bukkit API, not a fake "auto reel" -- shortens the
                // random wait-time window before a bite, so fish come
                // faster without pretending to auto-click for the player.
                int reduction = 100 * quickBite;
                event.getHook().setMinWaitTime(Math.max(20, 100 - reduction));
                event.getHook().setMaxWaitTime(Math.max(60, 600 - reduction));
            }
            castStartTime.put(player.getUniqueId(), System.currentTimeMillis());
            return;
        }

        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item caughtItem)) return;

        int sturdyHook = registry.getLevel(rod, "sturdy_hook");
        if (sturdyHook > 0 && JUNK_MATERIALS.contains(caughtItem.getItemStack().getType())
                && random.nextDouble() < 0.25 * sturdyHook) {
            caughtItem.setItemStack(new ItemStack(FISH_MATERIALS.get(random.nextInt(FISH_MATERIALS.size()))));
        }

        int doubleCatch = registry.getLevel(rod, "double_catch");
        if (doubleCatch > 0 && random.nextDouble() < 0.15 * doubleCatch) {
            ItemStack extra = caughtItem.getItemStack().clone();
            player.getWorld().dropItemNaturally(caughtItem.getLocation(), extra);
        }

        int seasonedAngler = registry.getLevel(rod, "anglers_luck");
        if (seasonedAngler > 0 && random.nextDouble() < 0.25 * seasonedAngler) {
            ExperienceOrb orb = player.getWorld().spawn(caughtItem.getLocation(), ExperienceOrb.class);
            orb.setExperience(2 * seasonedAngler);
        }

        int brineBlessed = registry.getLevel(rod, "brine_blessed");
        if (brineBlessed > 0 && player.getWorld().hasStorm() && random.nextDouble() < 0.25 * brineBlessed) {
            ExperienceOrb orb = player.getWorld().spawn(caughtItem.getLocation(), ExperienceOrb.class);
            orb.setExperience(2 * brineBlessed);
        }

        int deepDiver = registry.getLevel(rod, "deep_diver");
        if (deepDiver > 0 && caughtItem.getLocation().getY() < 50 && random.nextDouble() < 0.20 * deepDiver) {
            ExperienceOrb orb = player.getWorld().spawn(caughtItem.getLocation(), ExperienceOrb.class);
            orb.setExperience(2 * deepDiver);
        }

        int patientAngler = registry.getLevel(rod, "patient_angler");
        Long castStart = castStartTime.remove(player.getUniqueId());
        if (patientAngler > 0 && castStart != null) {
            long waitedMs = System.currentTimeMillis() - castStart;
            double bonusChance = Math.min(0.35, (waitedMs / 60_000.0) * 0.10 * patientAngler);
            if (random.nextDouble() < bonusChance) {
                ExperienceOrb orb = player.getWorld().spawn(caughtItem.getLocation(), ExperienceOrb.class);
                orb.setExperience(3 * patientAngler);
                player.sendMessage("§6Patient Angler: that wait paid off.");
            }
        }
    }
}
