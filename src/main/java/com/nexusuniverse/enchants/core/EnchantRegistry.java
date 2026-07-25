package com.nexusuniverse.enchants.core;

import com.nexusuniverse.enchants.NexusEnchantsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnchantRegistry {

    private final Map<String, CustomEnchant> byId = new LinkedHashMap<>();
    private final Map<String, NamespacedKey> keys = new LinkedHashMap<>();

    public EnchantRegistry(NexusEnchantsPlugin plugin) {
        for (CustomEnchant e : EnchantDefinitions.ALL) {
            byId.put(e.id(), e);
            keys.put(e.id(), new NamespacedKey(plugin, "ce_" + e.id()));
        }
    }

    public Collection<CustomEnchant> all() {
        return byId.values();
    }

    public CustomEnchant get(String id) {
        return byId.get(id);
    }

    public NamespacedKey keyFor(String id) {
        NamespacedKey key = keys.get(id);
        if (key == null) {
            throw new IllegalArgumentException("Unknown enchant id: " + id);
        }
        return key;
    }

    public int getLevel(ItemStack item, String id) {
        if (item == null || !item.hasItemMeta()) return 0;
        Integer level = item.getItemMeta().getPersistentDataContainer().get(keyFor(id), PersistentDataType.INTEGER);
        return level == null ? 0 : level;
    }

    /** Same idea as getLevel(ItemStack, id), but for anything with its own PDC directly -- e.g. an in-flight Arrow entity. */
    public int getLevel(org.bukkit.persistence.PersistentDataHolder holder, String id) {
        if (holder == null) return 0;
        Integer level = holder.getPersistentDataContainer().get(keyFor(id), PersistentDataType.INTEGER);
        return level == null ? 0 : level;
    }

    public void setLevel(org.bukkit.persistence.PersistentDataHolder holder, String id, int level) {
        holder.getPersistentDataContainer().set(keyFor(id), PersistentDataType.INTEGER, level);
    }

    /** Highest level of this enchant across all four armor slots (any one piece having it is enough). */
    public int maxArmorLevel(Player player, String id) {
        PlayerInventory inv = player.getInventory();
        return Math.max(
                Math.max(getLevel(inv.getHelmet(), id), getLevel(inv.getChestplate(), id)),
                Math.max(getLevel(inv.getLeggings(), id), getLevel(inv.getBoots(), id))
        );
    }

    public int mainHandLevel(Player player, String id) {
        return getLevel(player.getInventory().getItemInMainHand(), id);
    }
}
