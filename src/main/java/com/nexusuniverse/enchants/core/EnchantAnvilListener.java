package com.nexusuniverse.enchants.core;

import com.nexusuniverse.enchants.NexusEnchantsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * One listener handles every enchant in the registry -- adding a new
 * enchant to EnchantDefinitions doesn't require touching this file.
 * Combining a tome with an item that matches the enchant's category
 * (checked via CustomEnchant#appliesTo) tags that item with the level,
 * bumping it by one (capped at maxLevel) if it already has that exact
 * level, matching how vanilla lets you combine same-level enchants to
 * step up a level.
 *
 * Same anvil-UI caveat as the original Lava/Tide Walker scrolls: the
 * displayed XP-level cost isn't recalculated for this custom
 * combination and may not mean anything for what's actually happening --
 * the result item is correct regardless.
 */
public class EnchantAnvilListener implements Listener {

    private final EnchantRegistry registry;
    private final EnchantTomeItems tomeItems;

    public EnchantAnvilListener(NexusEnchantsPlugin plugin, EnchantRegistry registry, EnchantTomeItems tomeItems) {
        this.registry = registry;
        this.tomeItems = tomeItems;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack base = event.getInventory().getItem(0);
        ItemStack addition = event.getInventory().getItem(1);
        if (base == null || addition == null) return;

        EnchantTomeItems.TomeContents tome = tomeItems.readTome(addition);
        if (tome == null) return;

        CustomEnchant enchant = registry.get(tome.enchantId());
        if (enchant == null || !enchant.appliesTo().test(base.getType())) return;

        int currentLevel = registry.getLevel(base, enchant.id());
        int newLevel = currentLevel == tome.level()
                ? Math.min(enchant.maxLevel(), currentLevel + 1)
                : Math.max(currentLevel, tome.level());
        if (newLevel == currentLevel) return; // nothing would change -- don't offer a result

        ItemStack result = base.clone();
        ItemMeta meta = result.getItemMeta();
        meta.getPersistentDataContainer().set(registry.keyFor(enchant.id()), PersistentDataType.INTEGER, newLevel);
        meta.setEnchantmentGlintOverride(true);

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(enchant.displayName() + (enchant.maxLevel() > 1 ? " " + roman(newLevel) : ""));
        meta.setLore(lore);

        result.setItemMeta(meta);
        event.setResult(result);
    }

    private String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(n);
        };
    }
}
