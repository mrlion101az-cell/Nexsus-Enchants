package com.nexusuniverse.enchants.core;

/** Which slot/item-type family an enchant is meant for. */
public enum EnchantCategory {
    WEAPON,
    ARMOR,
    TOOL,
    BOW,
    FISHING_ROD,
    SHIELD,
    TRIDENT,
    SHEARS,
    ELYTRA,
    MACE,
    HORSE_ARMOR,
    COMPASS,
    TOTEM,
    SPYGLASS,
    CARVED_PUMPKIN,
    FIREWORK_ROCKET,
    LEAD,
    /** Crossbow-exclusive -- distinct from BOW, which still covers both bow and crossbow. */
    CROSSBOW,
    /** Applies to any item at all -- e.g. Soulbound. */
    UNIVERSAL
}
