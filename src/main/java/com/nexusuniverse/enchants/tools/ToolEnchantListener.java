package com.nexusuniverse.enchants.tools;

import com.nexusuniverse.enchants.core.EnchantRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Vein Miner and Treecapitator both use the same connected-same-material
 * flood fill (mineCluster), just with different starting predicates and
 * caps. Excavator mines a small area around the origin instead of
 * following connectivity. Tunnel drills a straight line in whatever
 * direction the player is facing instead of following either
 * connectivity or an area -- three genuinely different shapes for three
 * different tools, not the same mechanic renamed.
 *
 * Regrowth intercepts harvesting a fully-grown crop and replants it
 * instead of letting the block fully clear.
 *
 * Auto Smelt, Telepathy, Fortune+, and the two mining curses all apply
 * per-block inside breakOneBlock(), regardless of which path (single
 * block, vein, tree, area, or tunnel) triggered it, so they stack
 * correctly with each other and with whichever shape enchant fired.
 */
public class ToolEnchantListener implements Listener {

    private static final BlockFace[] NEIGHBORS = {
            BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    /** Tunnel stops here instead of chewing through world-critical blocks. */
    private static final Set<Material> UNBREAKABLE = Set.of(
            Material.BEDROCK, Material.BARRIER, Material.END_PORTAL_FRAME, Material.END_PORTAL,
            Material.END_GATEWAY, Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK, Material.STRUCTURE_BLOCK, Material.STRUCTURE_VOID,
            Material.JIGSAW, Material.LIGHT, Material.MOVING_PISTON
    );

    private final EnchantRegistry registry;
    private final Random random = new Random();

    public ToolEnchantListener(EnchantRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        Block origin = event.getBlock();
        Material originType = origin.getType();

        int reclaimer = registry.getLevel(tool, "reclaimer");
        if (reclaimer > 0 && originType == Material.SPAWNER) {
            event.setCancelled(true);
            reclaimSpawner(origin, player);
            return;
        }

        int regrowth = registry.getLevel(tool, "regrowth");
        if (regrowth > 0 && isMatureCrop(origin)) {
            event.setCancelled(true);
            harvestAndReplant(origin, player, tool);
            return;
        }

        // Keen Edge only applies to the plain single-block path below it --
        // the cluster/area/tunnel paths cancel this event and handle their
        // own drops manually, which means vanilla's XP-drop mechanism never
        // runs for them either, so there's nothing for this to add to.
        int keenEdge = registry.getLevel(tool, "keen_edge");
        if (keenEdge > 0) {
            event.setExpToDrop(event.getExpToDrop() + keenEdge);
        }

        int veinMiner = registry.getLevel(tool, "vein_miner");
        int treecapitator = registry.getLevel(tool, "treecapitator");
        int excavator = registry.getLevel(tool, "excavator");
        int tunnel = registry.getLevel(tool, "tunnel");

        if (veinMiner > 0 && isAnyOre(originType)) {
            event.setCancelled(true); // we're breaking the whole cluster ourselves
            mineCluster(origin, originType, 16 * veinMiner, player, tool);
            return;
        }
        if (treecapitator > 0 && Tag.LOGS.isTagged(originType)) {
            event.setCancelled(true);
            mineCluster(origin, originType, 24 * treecapitator, player, tool);
            return;
        }
        if (excavator > 0 && isDirtLike(originType)) {
            event.setCancelled(true);
            mineArea(origin, originType, excavator, player, tool);
            return;
        }
        if (tunnel > 0) {
            event.setCancelled(true);
            mineTunnel(origin, player, tool, tunnel);
            return;
        }

        // No cluster/area/tunnel enchant fired -- still apply per-block
        // enchants (Auto Smelt, Telepathy, Fortune+, curses) to this single block.
        applyPerBlockEnchants(event, origin, player, tool);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack tool = player.getInventory().getItemInMainHand();

        int prospector = registry.getLevel(tool, "prospector");
        if (prospector > 0) {
            revealNearbyOres(player);
        }

        int surveyor = registry.getLevel(tool, "surveyor");
        if (surveyor > 0) {
            int y = player.getLocation().getBlockY();
            int belowSeaLevel = 62 - y;
            player.sendMessage("§bSurveyor: §fY=" + y
                    + (belowSeaLevel > 0 ? " §7(" + belowSeaLevel + " blocks below sea level)" : ""));
        }
    }

    @EventHandler
    public void onItemDamage(org.bukkit.event.player.PlayerItemDamageEvent event) {
        int efficientStrikes = registry.getLevel(event.getItem(), "efficient_strikes");
        if (efficientStrikes > 0) {
            event.setDamage(Math.max(0, event.getDamage() - efficientStrikes));
        }

        int reinforced = registry.getLevel(event.getItem(), "reinforced");
        if (reinforced > 0 && random.nextDouble() < 0.10 * reinforced) {
            event.setDamage(0);
        }
    }

    private void revealNearbyOres(Player player) {
        Location center = player.getLocation();
        int radius = 12;
        int found = 0;
        outer:
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (found >= 40) break outer; // cap the ping so a dense area doesn't flood particles
                    Block block = center.getBlock().getRelative(dx, dy, dz);
                    if (isAnyOre(block.getType())) {
                        player.spawnParticle(Particle.END_ROD, block.getLocation().add(0.5, 0.5, 0.5), 3, 0.15, 0.15, 0.15, 0);
                        found++;
                    }
                }
            }
        }
        player.sendMessage("§eProspector: revealed " + found + " nearby ore block(s).");
    }

    private void reclaimSpawner(Block block, Player player) {
        ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
        ItemMeta meta = spawnerItem.getItemMeta();
        if (block.getState() instanceof CreatureSpawner spawnerState && meta instanceof BlockStateMeta blockStateMeta) {
            EntityType spawnedType = spawnerState.getSpawnedType();
            CreatureSpawner newState = (CreatureSpawner) blockStateMeta.getBlockState();
            if (spawnedType != null) {
                newState.setSpawnedType(spawnedType);
            }
            blockStateMeta.setBlockState(newState);
            spawnerItem.setItemMeta(blockStateMeta);
        }

        block.setType(Material.AIR, false);
        var leftover = player.getInventory().addItem(spawnerItem);
        leftover.values().forEach(item -> block.getWorld().dropItemNaturally(block.getLocation(), item));
    }

    private void mineCluster(Block origin, Material targetType, int maxBlocks, Player player, ItemStack tool) {
        Set<Block> visited = new HashSet<>();
        Deque<Block> queue = new ArrayDeque<>();
        List<Block> toBreak = new ArrayList<>();

        queue.add(origin);
        visited.add(origin);
        while (!queue.isEmpty() && toBreak.size() < maxBlocks) {
            Block current = queue.poll();
            toBreak.add(current);
            for (BlockFace face : NEIGHBORS) {
                Block neighbor = current.getRelative(face);
                if (visited.add(neighbor) && neighbor.getType() == targetType) {
                    queue.add(neighbor);
                }
            }
        }

        for (Block block : toBreak) {
            breakOneBlock(block, player, tool);
        }
    }

    private void mineArea(Block origin, Material targetType, int level, Player player, ItemStack tool) {
        int radius = level; // level 1 = 3x3, level 2 = 5x5, level 3 = 7x7, single Y layer
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block block = origin.getRelative(dx, 0, dz);
                if (block.getType() == targetType) {
                    breakOneBlock(block, player, tool);
                }
            }
        }
    }

    private void mineTunnel(Block origin, Player player, ItemStack tool, int level) {
        breakOneBlock(origin, player, tool);

        BlockFace facing = player.getFacing();
        int length = 4 * level;
        Block current = origin;
        for (int i = 0; i < length; i++) {
            current = current.getRelative(facing);
            Material type = current.getType();
            if (type == Material.AIR || UNBREAKABLE.contains(type)) {
                break; // hit open air or something that was never meant to be mined -- stop the tunnel here
            }
            breakOneBlock(current, player, tool);
        }
    }

    private void harvestAndReplant(Block block, Player player, ItemStack tool) {
        Collection<ItemStack> drops = block.getDrops(tool);
        for (ItemStack drop : drops) {
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
        }
        BlockData data = block.getBlockData();
        if (data instanceof Ageable ageable) {
            ageable.setAge(0);
            block.setBlockData(ageable, false);
        }
    }

    private void breakOneBlock(Block block, Player player, ItemStack tool) {
        Material type = block.getType();
        Collection<ItemStack> drops = block.getDrops(tool);
        block.setType(Material.AIR, false);

        int brightOre = registry.getLevel(tool, "bright_ore");
        if (brightOre > 0 && isAnyOre(type)) {
            block.getWorld().spawnParticle(org.bukkit.Particle.GLOW,
                    block.getLocation().add(0.5, 0.5, 0.5), 10 * brightOre, 0.3, 0.3, 0.3, 0);
        }

        int prospectorsCharm = registry.getLevel(tool, "prospectors_charm");
        if (prospectorsCharm > 0 && random.nextDouble() < 0.02 * prospectorsCharm) {
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.EMERALD));
        }

        int curseOfFragility = registry.getLevel(tool, "curse_of_brittleness");
        if (curseOfFragility > 0) {
            damageToolExtra(tool, curseOfFragility);
        }

        int curseOfMisfortune = registry.getLevel(tool, "curse_of_ruin");
        if (curseOfMisfortune > 0 && random.nextDouble() < 0.10 * curseOfMisfortune) {
            int salvager = registry.getLevel(tool, "salvager");
            if (salvager == 0 || random.nextDouble() >= 0.5 * salvager) {
                return; // drops destroyed entirely -- nothing further to give the player
            }
            // Salvager rolled a save -- fall through to normal drop handling below.
        }

        Material smelted = OreSmeltMap.smeltedFormOf(type);
        Material cut = StonecutterMap.cutFormOf(type);
        List<ItemStack> finalDrops = new ArrayList<>();
        int stonecutterTouch = registry.getLevel(tool, "stonecutter_touch");
        for (ItemStack drop : drops) {
            if (smelted != null) {
                finalDrops.add(new ItemStack(smelted, drop.getAmount()));
            } else if (stonecutterTouch > 0 && cut != null) {
                finalDrops.add(new ItemStack(cut, drop.getAmount()));
            } else {
                finalDrops.add(drop);
            }
        }

        int fortunePlus = registry.getLevel(tool, "fortune_plus");
        if (fortunePlus > 0 && !finalDrops.isEmpty() && random.nextDouble() < 0.15 * fortunePlus) {
            finalDrops.add(finalDrops.get(0).clone());
        }

        int telepathy = registry.getLevel(tool, "telepathy");
        for (ItemStack drop : finalDrops) {
            if (telepathy > 0) {
                var leftover = player.getInventory().addItem(drop);
                leftover.values().forEach(item -> block.getWorld().dropItemNaturally(block.getLocation(), item));
            } else {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }
    }

    private void applyPerBlockEnchants(BlockBreakEvent event, Block block, Player player, ItemStack tool) {
        int autoSmelt = registry.getLevel(tool, "auto_smelt");
        int telepathy = registry.getLevel(tool, "telepathy");
        int fortunePlus = registry.getLevel(tool, "fortune_plus");
        int stonecutterTouch = registry.getLevel(tool, "stonecutter_touch");
        int curseOfFragility = registry.getLevel(tool, "curse_of_brittleness");
        int curseOfMisfortune = registry.getLevel(tool, "curse_of_ruin");
        if (autoSmelt == 0 && telepathy == 0 && fortunePlus == 0 && stonecutterTouch == 0
                && curseOfFragility == 0 && curseOfMisfortune == 0) {
            return; // let vanilla handle it untouched
        }

        event.setDropItems(false);
        breakOneBlock(block, player, tool);
    }

    /** Curse of Fragility: extra durability loss beyond vanilla's normal 1-per-use. */
    private void damageToolExtra(ItemStack tool, int level) {
        if (tool == null) return;
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return;
        damageable.setDamage(damageable.getDamage() + level);
        tool.setItemMeta(meta);
    }

    private boolean isAnyOre(Material material) {
        return material.name().endsWith("_ORE") || material == Material.ANCIENT_DEBRIS;
    }

    private boolean isDirtLike(Material material) {
        return switch (material) {
            case DIRT, GRASS_BLOCK, COARSE_DIRT, ROOTED_DIRT, SAND, RED_SAND, GRAVEL,
                    CLAY, MYCELIUM, PODZOL, SOUL_SAND, SOUL_SOIL -> true;
            default -> false;
        };
    }

    private boolean isMatureCrop(Block block) {
        BlockData data = block.getBlockData();
        return data instanceof Ageable ageable && ageable.getAge() >= ageable.getMaximumAge();
    }
}
