package com.nexusuniverse.enchants.walk;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ElementalWalkManager {

    private static final double SINK_CORRECTION_THRESHOLD = 0.05;

    private final ElementalWalkItems items;

    public ElementalWalkManager(ElementalWalkItems items) {
        this.items = items;
    }

    /** Called every tick from the central loop for each online player. */
    public void tick(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        boolean hasLava = items.hasAbility(boots, ElementalType.LAVA);
        boolean hasWater = items.hasAbility(boots, ElementalType.WATER);
        if (!hasLava && !hasWater) return;

        Location loc = player.getLocation();
        Block feetBlock = loc.getBlock();
        Material feetType = feetBlock.getType();

        if (hasLava && feetType == Material.LAVA) {
            holdOnSurface(player, feetBlock);
            player.setFireTicks(0);
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, true, false));
        } else if (hasWater && feetType == Material.WATER && !player.isSneaking()) {
            holdOnSurface(player, feetBlock);
        }
    }

    /**
     * Keeps the player riding the top surface of the liquid block instead
     * of sinking into it. This is a per-tick position/velocity correction,
     * not a real collision change -- the plugin API doesn't expose liquid
     * collision the way it does solid blocks, so under lag or fast
     * movement there can be a very slight bob/jitter right at the
     * surface, unlike walking on an actual solid block. A perfectly
     * seamless version of this would need NMS or a mixin -- a different
     * scale of project than a plugin, and not attempted here.
     *
     * Sneaking is left as an override in tick() above (checked before
     * this is even called for water) so players can still choose to dive.
     */
    private void holdOnSurface(Player player, Block liquidBlock) {
        double surfaceY = liquidBlock.getY() + 1.0;
        Location loc = player.getLocation();

        Vector velocity = player.getVelocity();
        if (velocity.getY() < 0) {
            player.setVelocity(new Vector(velocity.getX(), 0, velocity.getZ()));
        }

        if (surfaceY - loc.getY() > SINK_CORRECTION_THRESHOLD) {
            loc.setY(surfaceY);
            player.teleport(loc);
        }
    }
}
