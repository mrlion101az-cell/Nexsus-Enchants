package com.nexusuniverse.enchants.walk;

import com.nexusuniverse.enchants.NexusEnchantsPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Combining a walker scroll with any pair of boots at an anvil tags
 * those boots with the ability, keeping their existing name, lore, and
 * real enchantments intact -- this is the "make it feel like a real
 * enchantment" path, as opposed to just handing out pre-made boots.
 *
 * One honest limitation: the anvil UI's displayed XP-level cost isn't
 * recalculated for this custom combination -- PrepareAnvilEvent doesn't
 * give plugins that level of control over the cost display, only over
 * the result item. It'll show whatever vanilla's default repair-cost
 * guess produces, which may not mean anything for this specific
 * combination. Cosmetic quirk in the UI only -- the result item itself
 * is correct regardless of what cost number is shown.
 */
public class ElementalWalkAnvilListener implements Listener {

    private final NexusEnchantsPlugin plugin;

    public ElementalWalkAnvilListener(NexusEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack base = event.getInventory().getItem(0);
        ItemStack addition = event.getInventory().getItem(1);
        if (base == null || addition == null) return;
        if (!isBoots(base.getType())) return;

        ElementalType type = plugin.getElementalWalkItems().readScrollType(addition);
        if (type == null) return;

        ItemStack result = base.clone();
        ItemMeta meta = result.getItemMeta();
        meta.getPersistentDataContainer().set(
                plugin.getElementalWalkItems().keyFor(type), PersistentDataType.BOOLEAN, true);
        meta.setEnchantmentGlintOverride(true);

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(type == ElementalType.LAVA
                ? "§7Walk on lava without sinking or burning."
                : "§7Walk on water without sinking. Sneak to dive.");
        meta.setLore(lore);

        result.setItemMeta(meta);
        event.setResult(result);
    }

    private boolean isBoots(Material material) {
        return material.name().endsWith("_BOOTS");
    }
}
