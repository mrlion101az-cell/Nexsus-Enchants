package com.nexusuniverse.enchants.passive;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PassiveEffectManager {

    private static final List<PotionEffectType> NEGATIVE_EFFECTS = List.of(
            PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.SLOWNESS,
            PotionEffectType.WEAKNESS, PotionEffectType.NAUSEA, PotionEffectType.BLINDNESS,
            PotionEffectType.MINING_FATIGUE, PotionEffectType.HUNGER
    );
    private static final long CLEAN_SLATE_COOLDOWN_MS = 30_000;

    private final EnchantRegistry registry;
    private final Random random = new Random();
    private final Map<UUID, Long> cleanSlateCooldown = new HashMap<>();
    private final Map<UUID, Biome> lastBiome = new HashMap<>();

    public PassiveEffectManager(EnchantRegistry registry) {
        this.registry = registry;
    }

    /** Called once per second from the central tick loop, for every online player. */
    public void tick(Player player) {
        if (registry.maxArmorLevel(player, "nightsight") > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 260, 0, true, false));
        }

        int magnetism = registry.maxArmorLevel(player, "magnetism");
        if (magnetism > 0) {
            double radius = 2.0 * magnetism;
            for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
                if (!(nearby instanceof Item item)) continue;
                Vector direction = player.getLocation().toVector().subtract(item.getLocation().toVector());
                if (direction.lengthSquared() < 0.01) continue;
                item.setVelocity(direction.normalize().multiply(0.3));
            }
        }

        int waterborne = registry.maxArmorLevel(player, "waterborne");
        if (waterborne > 0 && player.isInWater()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, true, false));
        }

        int hasteAura = registry.mainHandLevel(player, "haste_aura");
        if (hasteAura > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, hasteAura - 1, true, false));
        }

        int cure = registry.maxArmorLevel(player, "cure");
        if (cure > 0 && random.nextDouble() < 0.10 * cure) {
            for (PotionEffectType negative : NEGATIVE_EFFECTS) {
                if (player.hasPotionEffect(negative)) {
                    player.removePotionEffect(negative);
                    break; // one effect per successful roll, not a full cleanse every time
                }
            }
        }

        int resilience = registry.maxArmorLevel(player, "resilience");
        if (resilience > 0) {
            PotionEffect current = player.getPotionEffect(PotionEffectType.ABSORPTION);
            int currentAmplifier = current != null ? current.getAmplifier() : -1;
            int cap = resilience - 1; // level 1 -> amplifier 0 (one heart), level 3 -> amplifier 2
            if (currentAmplifier < cap) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, currentAmplifier + 1, true, false));
            }
        }

        int deepReach = registry.mainHandLevel(player, "deep_reach");
        if (deepReach > 0) {
            int depthBelowSeaLevel = Math.max(0, 62 - player.getLocation().getBlockY());
            int amplifier = Math.min(deepReach - 1 + depthBelowSeaLevel / 16, 4);
            if (amplifier >= 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, amplifier, true, false));
            }
        }

        int landlocked = registry.mainHandLevel(player, "curse_of_the_landlocked");
        if (landlocked > 0 && !player.isInWater()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, landlocked - 1, true, false));
        }

        ItemStack chest = player.getInventory().getChestplate();

        int glideBoost = registry.getLevel(chest, "glide_boost");
        if (glideBoost > 0 && player.isGliding()) {
            Vector forward = player.getLocation().getDirection().setY(0).normalize().multiply(0.02 * glideBoost);
            player.setVelocity(player.getVelocity().add(forward));
        }

        int cloudburst = registry.getLevel(chest, "cloudburst");
        if (cloudburst > 0 && player.isGliding() && player.getWorld().hasStorm() && random.nextDouble() < 0.3) {
            Vector boost = player.getLocation().getDirection().normalize().multiply(0.05 * cloudburst);
            player.setVelocity(player.getVelocity().add(boost));
        }

        int skyDiver = registry.getLevel(chest, "sky_diver");
        if (skyDiver > 0 && !player.isGliding() && !player.isOnGround()
                && player.getFallDistance() > 0 && player.getFallDistance() < 4
                && !player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 30, 0, true, false));
        }

        int swiftStride = registry.maxArmorLevel(player, "swift_stride");
        if (swiftStride > 0 && player.isSprinting()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, swiftStride - 1, true, true));
        }

        int quicksilver = registry.mainHandLevel(player, "quicksilver");
        if (quicksilver > 0 && player.isInWater()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, quicksilver - 1, true, false));
        }

        int currentRider = registry.mainHandLevel(player, "current_rider");
        if (currentRider > 0 && player.isInWater()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, currentRider - 1, true, false));
        }

        int stormrider = registry.mainHandLevel(player, "stormrider");
        if (stormrider > 0 && player.getWorld().hasStorm()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, stormrider - 1, true, false));
        }

        if (registry.maxArmorLevel(player, "unshakeable") > 0) {
            if (player.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
            }
            if (player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
                player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            }
        }

        ItemStack compass = player.getInventory().getItemInMainHand();
        int pathfinder = registry.getLevel(compass, "pathfinder");
        int wandererCurse = registry.getLevel(compass, "curse_of_the_wanderer");
        if (pathfinder > 0 || wandererCurse > 0) {
            Player nearest = findNearestOtherPlayer(player, 200);
            if (nearest != null) {
                if (pathfinder > 0) {
                    player.setCompassTarget(nearest.getLocation());
                } else {
                    Vector away = player.getLocation().toVector().subtract(nearest.getLocation().toVector());
                    if (away.lengthSquared() < 0.01) away = new Vector(1, 0, 0);
                    Location fakeTarget = player.getLocation().add(away.normalize().multiply(500));
                    player.setCompassTarget(fakeTarget);
                }
            }
        }

        int bountyFinder = registry.getLevel(compass, "bounty_finder");
        if (bountyFinder > 0) {
            org.bukkit.entity.Monster nearestMonster = findNearestMonster(player, 100);
            if (nearestMonster != null) {
                player.setCompassTarget(nearestMonster.getLocation());
            }
        }

        int surveyorsEye = registry.getLevel(compass, "surveyors_eye");
        if (surveyorsEye > 0) {
            Biome currentBiome = player.getLocation().getBlock().getBiome();
            Biome previousBiome = lastBiome.put(player.getUniqueId(), currentBiome);
            if (previousBiome != null && !previousBiome.equals(currentBiome)) {
                player.sendMessage("§aSurveyor's Eye: entering " + formatBiomeName(currentBiome) + ".");
            }
        }

        int juggernaut = registry.maxArmorLevel(player, "juggernaut");
        if (juggernaut > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 40, juggernaut - 1, true, false));
        }

        if (registry.maxArmorLevel(player, "warm_heart") > 0 && player.hasPotionEffect(PotionEffectType.WITHER)) {
            player.removePotionEffect(PotionEffectType.WITHER);
        }

        int buoyant = registry.maxArmorLevel(player, "buoyant");
        if (buoyant > 0 && player.isInWater() && !player.isSneaking()) {
            Vector v = player.getVelocity();
            if (v.getY() < 0) {
                player.setVelocity(new Vector(v.getX(), Math.max(v.getY(), -0.02), v.getZ()));
            }
        }

        if (registry.maxArmorLevel(player, "iron_will") > 0 && player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
            player.removePotionEffect(PotionEffectType.WEAKNESS);
        }

        if (registry.maxArmorLevel(player, "clear_mind") > 0 && player.hasPotionEffect(PotionEffectType.NAUSEA)) {
            player.removePotionEffect(PotionEffectType.NAUSEA);
        }

        int cleanSlate = registry.maxArmorLevel(player, "clean_slate");
        if (cleanSlate > 0) {
            long now = System.currentTimeMillis();
            Long last = cleanSlateCooldown.get(player.getUniqueId());
            if (last == null || now - last > CLEAN_SLATE_COOLDOWN_MS) {
                boolean hadNegative = false;
                for (PotionEffectType negative : NEGATIVE_EFFECTS) {
                    if (player.hasPotionEffect(negative)) {
                        player.removePotionEffect(negative);
                        hadNegative = true;
                    }
                }
                if (hadNegative) {
                    cleanSlateCooldown.put(player.getUniqueId(), now);
                }
            }
        }

        int nightOwl = registry.getLevel(player.getInventory().getHelmet(), "night_owl");
        if (nightOwl > 0) {
            long time = player.getWorld().getTime();
            if (time >= 13000 && time <= 23000) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 40, 0, true, false));
            }
        }

        int scarecrowCurse = registry.getLevel(player.getInventory().getHelmet(), "curse_of_the_scarecrow");
        if (scarecrowCurse > 0) {
            double radius = 10.0 * scarecrowCurse;
            for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
                if (nearby instanceof org.bukkit.entity.Mob mob && nearby instanceof org.bukkit.entity.Monster
                        && mob.getTarget() == null) {
                    mob.setTarget(player);
                }
            }
        }

        int heavyShieldCurse = Math.max(
                registry.getLevel(player.getInventory().getItemInMainHand(), "curse_of_the_heavy_shield"),
                registry.getLevel(player.getInventory().getItemInOffHand(), "curse_of_the_heavy_shield"));
        if (heavyShieldCurse > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, heavyShieldCurse - 1, true, false));
        }

        int nightCrew = registry.mainHandLevel(player, "night_crew");
        if (nightCrew > 0) {
            long time = player.getWorld().getTime();
            boolean isNight = time >= 13000 && time <= 23000;
            if (isNight) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, nightCrew - 1, true, false));
            }
        }

        ItemStack spyglass = player.getInventory().getItemInMainHand();
        int keenSight = registry.getLevel(spyglass, "keen_sight");
        if (keenSight > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 40, 0, true, false));
        }

        int mobSense = registry.getLevel(spyglass, "mob_sense");
        if (mobSense > 0) {
            double radius = 10.0 * mobSense;
            for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
                if (nearby instanceof org.bukkit.entity.Monster monster) {
                    monster.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, true, false));
                }
            }
        }

        int dimSight = registry.getLevel(spyglass, "curse_of_dim_sight");
        if (dimSight > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, dimSight - 1, true, false));
        }

        // Loot Sense doesn't un-glow items once they've been seen -- there's
        // no cheap "did I already mark this one" check without extra
        // bookkeeping, so an item can stay glowing after you walk away or
        // put the spyglass down. Cosmetic-only side effect, not a bug that
        // affects gameplay, but worth knowing about.
        int lootSense = registry.getLevel(spyglass, "loot_sense");
        if (lootSense > 0) {
            double radius = 15.0 * lootSense;
            for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
                if (nearby instanceof Item item) {
                    item.setGlowing(true);
                }
            }
        }

        tryRepair(player.getInventory().getItemInMainHand());
        tryRepair(player.getInventory().getItemInOffHand());
        tryRepair(player.getInventory().getHelmet());
        tryRepair(player.getInventory().getChestplate());
        tryRepair(player.getInventory().getLeggings());
        tryRepair(player.getInventory().getBoots());
    }

    /** Reforged: slowly repairs the item it's on. Scoped to what's equipped/held, not the whole inventory. */
    private void tryRepair(ItemStack item) {
        if (item == null) return;
        int reforged = registry.getLevel(item, "reforged");
        if (reforged == 0) return;

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable) || damageable.getDamage() <= 0) return;

        damageable.setDamage(Math.max(0, damageable.getDamage() - reforged));
        item.setItemMeta(meta);
    }

    private Player findNearestOtherPlayer(Player from, double maxDistance) {
        Player nearest = null;
        double nearestDistanceSquared = maxDistance * maxDistance;
        for (Player candidate : Bukkit.getOnlinePlayers()) {
            if (candidate.equals(from) || !candidate.getWorld().equals(from.getWorld())) continue;
            double distanceSquared = candidate.getLocation().distanceSquared(from.getLocation());
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private org.bukkit.entity.Monster findNearestMonster(Player from, double radius) {
        org.bukkit.entity.Monster nearest = null;
        double nearestDistanceSquared = radius * radius;
        for (Entity nearby : from.getNearbyEntities(radius, radius, radius)) {
            if (!(nearby instanceof org.bukkit.entity.Monster monster)) continue;
            double distanceSquared = monster.getLocation().distanceSquared(from.getLocation());
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearest = monster;
            }
        }
        return nearest;
    }

    private String formatBiomeName(Biome biome) {
        String[] words = biome.name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
