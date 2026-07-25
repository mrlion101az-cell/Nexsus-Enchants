package com.nexusuniverse.enchants.tools;

import org.bukkit.Material;

import java.util.Map;

/**
 * A stylized approximation of real stonecutter recipes, not an exact
 * replication -- e.g. real Deepslate needs to become Cobbled Deepslate
 * before a stonecutter can turn it into Polished Deepslate, but this
 * plugin's version skips that intermediate step for simplicity. Close
 * enough for the "comes out already cut" feel this enchant is going for.
 */
public final class StonecutterMap {

    private StonecutterMap() {}

    private static final Map<Material, Material> CUT_RESULTS = Map.ofEntries(
            Map.entry(Material.STONE, Material.STONE_BRICKS),
            Map.entry(Material.DEEPSLATE, Material.POLISHED_DEEPSLATE),
            Map.entry(Material.COBBLED_DEEPSLATE, Material.POLISHED_DEEPSLATE),
            Map.entry(Material.ANDESITE, Material.POLISHED_ANDESITE),
            Map.entry(Material.DIORITE, Material.POLISHED_DIORITE),
            Map.entry(Material.GRANITE, Material.POLISHED_GRANITE),
            Map.entry(Material.BLACKSTONE, Material.POLISHED_BLACKSTONE),
            Map.entry(Material.TUFF, Material.POLISHED_TUFF)
    );

    public static Material cutFormOf(Material raw) {
        return CUT_RESULTS.get(raw);
    }
}
