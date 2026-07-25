package com.nexusuniverse.enchants.walk;

import com.nexusuniverse.enchants.NexusEnchantsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Creates and detects the two "walker" abilities. These aren't real
 * vanilla Enchantment objects -- registering a genuinely new Enchantment
 * is fragile across Minecraft versions and not something plugin code
 * can do reliably on modern Paper. Instead this is a
 * PersistentDataContainer tag on a pair of boots, with the enchant
 * *glint* applied cosmetically via ItemMeta#setEnchantmentGlintOverride
 * (a real Paper API addition) so it still looks and behaves like an
 * enchantment in every way a player actually experiences, without the
 * fragility of faking a real Enchantment registration.
 */
public class ElementalWalkItems {

    private final NamespacedKey lavaWalkerKey;
    private final NamespacedKey waterWalkerKey;
    private final NamespacedKey scrollKey; // value = ElementalType name

    public ElementalWalkItems(NexusEnchantsPlugin plugin) {
        this.lavaWalkerKey = new NamespacedKey(plugin, "lava_walker");
        this.waterWalkerKey = new NamespacedKey(plugin, "water_walker");
        this.scrollKey = new NamespacedKey(plugin, "elemental_walk_scroll");
    }

    public NamespacedKey keyFor(ElementalType type) {
        return type == ElementalType.LAVA ? lavaWalkerKey : waterWalkerKey;
    }

    public boolean hasAbility(ItemStack boots, ElementalType type) {
        if (boots == null || !boots.hasItemMeta()) return false;
        Boolean tag = boots.getItemMeta().getPersistentDataContainer().get(keyFor(type), PersistentDataType.BOOLEAN);
        return Boolean.TRUE.equals(tag);
    }

    public ItemStack createBoots(ElementalType type) {
        Material material = type == ElementalType.LAVA ? Material.NETHERITE_BOOTS : Material.DIAMOND_BOOTS;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(type == ElementalType.LAVA ? "§cLava Walker Boots" : "§bTide Walker Boots");
        meta.setLore(List.of(
                type == ElementalType.LAVA
                        ? "§7Walk on lava without sinking or burning."
                        : "§7Walk on water without sinking. Sneak to dive.",
                "§8(NexusEnchants ability -- not a real vanilla enchantment)"
        ));
        meta.getPersistentDataContainer().set(keyFor(type), PersistentDataType.BOOLEAN, true);
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createScroll(ElementalType type) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(type == ElementalType.LAVA ? "§cScroll of Lava Walking" : "§bScroll of Tide Walking");
        meta.setLore(List.of(
                "§7Combine with any boots at an anvil",
                "§7to grant this ability."
        ));
        meta.getPersistentDataContainer().set(scrollKey, PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        return item;
    }

    public ElementalType readScrollType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String raw = item.getItemMeta().getPersistentDataContainer().get(scrollKey, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return ElementalType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
