package com.nexusuniverse.enchants.core;

import com.nexusuniverse.enchants.NexusEnchantsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * One tome (enchanted book) grants one enchant at one level when
 * combined with a matching item at an anvil. The tome itself carries its
 * target enchant id and level as two PDC values (kept separate from the
 * per-enchant level keys in EnchantRegistry, since a tome isn't a
 * finished enchanted item -- it's an ingredient).
 */
public class EnchantTomeItems {

    private final EnchantRegistry registry;
    private final NamespacedKey tomeIdKey;
    private final NamespacedKey tomeLevelKey;

    public EnchantTomeItems(NexusEnchantsPlugin plugin, EnchantRegistry registry) {
        this.registry = registry;
        this.tomeIdKey = new NamespacedKey(plugin, "tome_enchant_id");
        this.tomeLevelKey = new NamespacedKey(plugin, "tome_enchant_level");
    }

    public ItemStack createTome(String enchantId, int level) {
        CustomEnchant enchant = registry.get(enchantId);
        if (enchant == null) {
            throw new IllegalArgumentException("Unknown enchant id: " + enchantId);
        }
        int clampedLevel = Math.max(1, Math.min(enchant.maxLevel(), level));

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(enchant.displayName() + (enchant.maxLevel() > 1 ? " " + roman(clampedLevel) : "") + " §7Tome");
        meta.setLore(List.of(
                "§7" + enchant.description(),
                "§8Combine with a matching item at an anvil."
        ));
        meta.getPersistentDataContainer().set(tomeIdKey, PersistentDataType.STRING, enchantId);
        meta.getPersistentDataContainer().set(tomeLevelKey, PersistentDataType.INTEGER, clampedLevel);
        item.setItemMeta(meta);
        return item;
    }

    /** Returns null if the item isn't a tome. */
    public TomeContents readTome(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String id = pdc.get(tomeIdKey, PersistentDataType.STRING);
        Integer level = pdc.get(tomeLevelKey, PersistentDataType.INTEGER);
        if (id == null || level == null) return null;
        return new TomeContents(id, level);
    }

    public record TomeContents(String enchantId, int level) {}

    private String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(n);
        };
    }
}
