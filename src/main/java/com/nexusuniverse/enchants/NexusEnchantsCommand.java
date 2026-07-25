package com.nexusuniverse.enchants;

import com.nexusuniverse.enchants.core.CustomEnchant;
import com.nexusuniverse.enchants.walk.ElementalType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NexusEnchantsCommand implements CommandExecutor {

    private final NexusEnchantsPlugin plugin;

    public NexusEnchantsCommand(NexusEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(player, args);
            case "list" -> handleList(player);
            default -> sendUsage(player);
        }
        return true;
    }

    private void handleGive(Player player, String[] args) {
        if (args.length < 2) {
            sendUsage(player);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "lavawalkerboots" ->
                    give(player, plugin.getElementalWalkItems().createBoots(ElementalType.LAVA), "Lava Walker Boots");
            case "waterwalkerboots" ->
                    give(player, plugin.getElementalWalkItems().createBoots(ElementalType.WATER), "Tide Walker Boots");
            case "lavawalkerscroll" ->
                    give(player, plugin.getElementalWalkItems().createScroll(ElementalType.LAVA), "Scroll of Lava Walking");
            case "waterwalkerscroll" ->
                    give(player, plugin.getElementalWalkItems().createScroll(ElementalType.WATER), "Scroll of Tide Walking");
            case "tome" -> handleGiveTome(player, args);
            default -> sendUsage(player);
        }
    }

    private void handleGiveTome(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /nexusenchants give tome <enchant_id> [level]");
            player.sendMessage("§7Run /nexusenchants list to see all enchant ids.");
            return;
        }
        String id = args[2].toLowerCase();
        CustomEnchant enchant = plugin.getEnchantRegistry().get(id);
        if (enchant == null) {
            player.sendMessage("§cUnknown enchant id: " + id + ". Run /nexusenchants list to see all ids.");
            return;
        }
        int level = 1;
        if (args.length >= 4) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cLevel must be a whole number.");
                return;
            }
        }
        ItemStack tome = plugin.getEnchantTomeItems().createTome(id, level);
        player.getInventory().addItem(tome);
        player.sendMessage("§aGiven: " + tome.getItemMeta().getDisplayName());
    }

    private void handleList(Player player) {
        player.sendMessage("§7--- NexusEnchants (" + plugin.getEnchantRegistry().all().size() + ") ---");
        for (CustomEnchant e : plugin.getEnchantRegistry().all()) {
            String curseTag = e.curse() ? " §4[CURSE]" : "";
            player.sendMessage(e.displayName() + curseTag + " §7(" + e.id() + ") §8[" + e.category() + ", max " + e.maxLevel() + "] §7- " + e.description());
        }
        player.sendMessage("§7Plus Lava Walker / Tide Walker -- see /nexusenchants give lavawalkerboots etc.");
    }

    private void give(Player player, ItemStack item, String label) {
        player.getInventory().addItem(item);
        player.sendMessage("§aGiven: " + label);
    }

    private void sendUsage(Player player) {
        player.sendMessage("§7/nexusenchants give <lavawalkerboots|waterwalkerboots|lavawalkerscroll|waterwalkerscroll>");
        player.sendMessage("§7/nexusenchants give tome <enchant_id> [level]");
        player.sendMessage("§7/nexusenchants list -- see every enchant id and what it does");
    }
}
