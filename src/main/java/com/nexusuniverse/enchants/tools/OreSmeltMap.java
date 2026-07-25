package com.nexusuniverse.enchants.tools;

import org.bukkit.Material;

import java.util.Map;

/** What Auto Smelt turns a mined block into, if anything. */
public final class OreSmeltMap {

    private OreSmeltMap() {}

    private static final Map<Material, Material> SMELT_RESULTS = Map.ofEntries(
            Map.entry(Material.IRON_ORE, Material.IRON_INGOT),
            Map.entry(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT),
            Map.entry(Material.GOLD_ORE, Material.GOLD_INGOT),
            Map.entry(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT),
            Map.entry(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT),
            Map.entry(Material.COPPER_ORE, Material.COPPER_INGOT),
            Map.entry(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT),
            Map.entry(Material.SAND, Material.GLASS),
            Map.entry(Material.RED_SAND, Material.GLASS),
            Map.entry(Material.COBBLESTONE, Material.STONE),
            Map.entry(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE),
            Map.entry(Material.CLAY, Material.TERRACOTTA)
    );

    /** Null if this block type has no smelted form (e.g. diamond/emerald ore -- already the final item). */
    public static Material smeltedFormOf(Material raw) {
        return SMELT_RESULTS.get(raw);
    }
}
