package com.nexusuniverse.enchants.core;

import org.bukkit.Material;

import java.util.function.Predicate;

/**
 * One custom enchant's static definition -- id, display, category, how
 * many levels it supports, which items it can go on, and a one-line
 * description used in tome lore. The actual trigger logic lives in the
 * category listeners (WeaponEnchantListener, ArmorDefenseListener,
 * ToolEnchantListener, ArrowEnchantListener, FishingEnchantListener,
 * PassiveEffectManager) -- this record is just the registry entry, not
 * the behavior itself.
 *
 * "curse" is purely informational for display (lore gets a red "Curse"
 * marker) -- curses don't need special unremovable-from-grindstone logic
 * the way vanilla curses do, because grindstone only strips real
 * Bukkit Enchantment objects, and none of these are one. A PDC tag is
 * already outside anything the grindstone touches, so every enchant in
 * this whole plugin is already grindstone-proof as a side effect of how
 * it's built -- not a curse-specific feature, just how it happens to work.
 */
public record CustomEnchant(
        String id,
        String displayName,
        EnchantCategory category,
        int maxLevel,
        Predicate<Material> appliesTo,
        String description,
        boolean curse
) {
}
