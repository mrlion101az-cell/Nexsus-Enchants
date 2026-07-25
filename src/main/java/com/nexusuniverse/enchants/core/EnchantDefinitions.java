package com.nexusuniverse.enchants.core;

import org.bukkit.Material;

import java.util.List;

/**
 * Wave 1 (26) + wave 2 (18) = 44 enchants across weapons, armor, tools,
 * bows/arrows, fishing rods, plus a handful of curses. Trigger logic for
 * each lives in the matching listener -- see WeaponEnchantListener,
 * ArmorDefenseListener, PassiveEffectManager, ToolEnchantListener,
 * ArrowEnchantListener, and FishingEnchantListener. Adding an enchant
 * later means one entry here plus whatever trigger logic it needs in the
 * matching listener -- the anvil/tome/registry machinery around it
 * doesn't need to change.
 */
public final class EnchantDefinitions {

    private EnchantDefinitions() {}

    private static boolean isSwordOrAxe(Material m) {
        return m.name().endsWith("_SWORD") || m.name().endsWith("_AXE");
    }

    private static boolean isArmor(Material m) {
        String n = m.name();
        return n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS");
    }

    private static boolean isMiningTool(Material m) {
        String n = m.name();
        return n.endsWith("_PICKAXE") || n.endsWith("_SHOVEL") || n.endsWith("_AXE") || n.endsWith("_HOE");
    }

    private static boolean isBow(Material m) {
        return m == Material.BOW || m == Material.CROSSBOW;
    }

    private static boolean isFishingRod(Material m) {
        return m == Material.FISHING_ROD;
    }

    private static boolean isAnyItem(Material m) {
        return m != Material.AIR;
    }

    private static boolean isShield(Material m) {
        return m == Material.SHIELD;
    }

    private static boolean isTrident(Material m) {
        return m == Material.TRIDENT;
    }

    private static boolean isShears(Material m) {
        return m == Material.SHEARS;
    }

    private static boolean isElytra(Material m) {
        return m == Material.ELYTRA;
    }

    private static boolean isMace(Material m) {
        return m == Material.MACE;
    }

    private static boolean isHorseArmor(Material m) {
        return m == Material.LEATHER_HORSE_ARMOR || m == Material.IRON_HORSE_ARMOR
                || m == Material.GOLDEN_HORSE_ARMOR || m == Material.DIAMOND_HORSE_ARMOR;
    }

    private static boolean isCompass(Material m) {
        return m == Material.COMPASS;
    }

    private static boolean isCrossbowOnly(Material m) {
        return m == Material.CROSSBOW;
    }

    private static boolean isTotem(Material m) {
        return m == Material.TOTEM_OF_UNDYING;
    }

    private static boolean isSpyglass(Material m) {
        return m == Material.SPYGLASS;
    }

    private static boolean isCarvedPumpkin(Material m) {
        return m == Material.CARVED_PUMPKIN;
    }

    private static boolean isFirework(Material m) {
        return m == Material.FIREWORK_ROCKET;
    }

    private static boolean isLead(Material m) {
        return m == Material.LEAD;
    }

    public static final List<CustomEnchant> ALL = List.of(

            // ---------- WEAPON (10) ----------
            new CustomEnchant("vampiric", "§4Vampiric", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Heal a % of damage dealt on hit.", false),
            new CustomEnchant("inferno", "§6Inferno", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Sets targets on fire longer than normal Fire Aspect.", false),
            new CustomEnchant("frostbite", "§bFrostbite", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Chance to slow targets on hit.", false),
            new CustomEnchant("venom", "§2Venom", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Chance to poison targets on hit.", false),
            new CustomEnchant("executioner", "§cExecutioner", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Bonus damage against targets below 20% health.", false),
            new CustomEnchant("bleed", "§4Bleed", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Targets take damage over time after being hit.", false),
            new CustomEnchant("soul_reaper", "§5Soul Reaper", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Extra damage to undead, small self-heal on kill-class hits.", false),
            new CustomEnchant("stun", "§eStun", EnchantCategory.WEAPON, 2,
                    EnchantDefinitions::isSwordOrAxe,
                    "Chance to briefly freeze a target in place.", false),
            new CustomEnchant("knockback_wave", "§9Knockback Wave", EnchantCategory.WEAPON, 2,
                    EnchantDefinitions::isSwordOrAxe,
                    "Hits knock back everyone near the target, not just the target.", false),
            new CustomEnchant("thunderstrike", "§eThunderstrike", EnchantCategory.WEAPON, 2,
                    EnchantDefinitions::isSwordOrAxe,
                    "Chance to strike a visual lightning effect on hit.", false),

            // ---------- ARMOR (12: 9 wave 1 + 3 wave 2) ----------
            new CustomEnchant("ironhide", "§7Ironhide", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Flat percentage damage reduction from any source.", false),
            new CustomEnchant("featherfall_plus", "§fFeatherfall+", EnchantCategory.ARMOR, 1,
                    EnchantDefinitions::isArmor,
                    "Completely negates fall damage.", false),
            new CustomEnchant("guardian", "§9Guardian", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Reduces damage taken from projectiles.", false),
            new CustomEnchant("insulation", "§bInsulation", EnchantCategory.ARMOR, 1,
                    EnchantDefinitions::isArmor,
                    "Immune to freezing/powder snow damage.", false),
            new CustomEnchant("second_wind", "§dSecond Wind", EnchantCategory.ARMOR, 1,
                    EnchantDefinitions::isArmor,
                    "Once per minute, survive lethal damage with a burst of Regeneration.", false),
            new CustomEnchant("nightsight", "§8Nightsight", EnchantCategory.ARMOR, 1,
                    EnchantDefinitions::isArmor,
                    "Permanent Night Vision while worn.", false),
            new CustomEnchant("magnetism", "§eMagnetism", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Pulls nearby dropped items toward you.", false),
            new CustomEnchant("waterborne", "§3Waterborne", EnchantCategory.ARMOR, 1,
                    EnchantDefinitions::isArmor,
                    "Water Breathing and faster underwater mining while submerged.", false),
            new CustomEnchant("thorns_plus", "§cThorns+", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Reflects a percentage of melee damage back at the attacker.", false),
            new CustomEnchant("cure", "§dCure", EnchantCategory.ARMOR, 2,
                    EnchantDefinitions::isArmor,
                    "Periodic chance to remove one negative potion effect.", false),
            new CustomEnchant("wisdom", "§bWisdom", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Increases experience gained from all sources.", false),
            new CustomEnchant("soulbound", "§5Soulbound", EnchantCategory.UNIVERSAL, 1,
                    EnchantDefinitions::isAnyItem,
                    "This item stays with you through death instead of dropping.", false),

            // ---------- TOOL (9: 7 wave 1 + 2 wave 2) ----------
            new CustomEnchant("auto_smelt", "§6Auto Smelt", EnchantCategory.TOOL, 1,
                    EnchantDefinitions::isMiningTool,
                    "Ores and select blocks come out already smelted.", false),
            new CustomEnchant("vein_miner", "§dVein Miner", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Mining one ore block mines the whole connected vein.", false),
            new CustomEnchant("treecapitator", "§2Treecapitator", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Chopping one log chops the whole connected tree.", false),
            new CustomEnchant("excavator", "§7Excavator", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Mining dirt/sand/gravel-type blocks clears a small area around it.", false),
            new CustomEnchant("fortune_plus", "§aFortune+", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Chance at bonus duplicate drops, stacking with real Fortune.", false),
            new CustomEnchant("telepathy", "§5Telepathy", EnchantCategory.TOOL, 1,
                    EnchantDefinitions::isMiningTool,
                    "Mined blocks go straight to your inventory instead of dropping.", false),
            new CustomEnchant("haste_aura", "§eHaste Aura", EnchantCategory.TOOL, 2,
                    EnchantDefinitions::isMiningTool,
                    "Grants Haste while the tool is held.", false),
            new CustomEnchant("tunnel", "§7Tunnel", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Mines straight ahead in the direction you're facing.", false),
            new CustomEnchant("regrowth", "§aRegrowth", EnchantCategory.TOOL, 1,
                    EnchantDefinitions::isMiningTool,
                    "Harvesting a fully-grown crop replants it immediately (hoe).", false),

            // ---------- BOW (6, new category) ----------
            new CustomEnchant("explosive_arrows", "§cExplosive Arrows", EnchantCategory.BOW, 3,
                    EnchantDefinitions::isBow,
                    "Arrows explode on impact (does not break blocks).", false),
            new CustomEnchant("poisoned_arrows", "§2Poisoned Arrows", EnchantCategory.BOW, 3,
                    EnchantDefinitions::isBow,
                    "Arrows poison whatever they hit.", false),
            new CustomEnchant("vampiric_arrows", "§4Vampiric Arrows", EnchantCategory.BOW, 3,
                    EnchantDefinitions::isBow,
                    "Heal a % of arrow damage dealt.", false),
            new CustomEnchant("confusing_arrows", "§dConfusing Arrows", EnchantCategory.BOW, 2,
                    EnchantDefinitions::isBow,
                    "Arrows apply Nausea on hit.", false),
            new CustomEnchant("blink_shot", "§5Blink Shot", EnchantCategory.BOW, 1,
                    EnchantDefinitions::isBow,
                    "Teleports you to wherever your arrow lands.", false),
            new CustomEnchant("longshot", "§eLongshot", EnchantCategory.BOW, 3,
                    EnchantDefinitions::isBow,
                    "Bonus damage that scales with how far the arrow traveled.", false),

            // ---------- FISHING ROD (3, new category) ----------
            new CustomEnchant("double_catch", "§bDouble Catch", EnchantCategory.FISHING_ROD, 2,
                    EnchantDefinitions::isFishingRod,
                    "Chance to catch two items at once.", false),
            new CustomEnchant("anglers_luck", "§6Angler's Luck", EnchantCategory.FISHING_ROD, 3,
                    EnchantDefinitions::isFishingRod,
                    "Chance at bonus experience per catch.", false),
            new CustomEnchant("quick_bite", "§bQuick Bite", EnchantCategory.FISHING_ROD, 3,
                    EnchantDefinitions::isFishingRod,
                    "Fish bite noticeably faster.", false),

            // ---------- CURSES (4, new -- deliberately negative) ----------
            new CustomEnchant("curse_of_brittleness", "§4Curse of Brittleness", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "The tool loses extra durability with every use.", true),
            new CustomEnchant("curse_of_ruin", "§4Curse of Ruin", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Chance for a mined block's drops to be destroyed instead of dropping.", true),
            new CustomEnchant("curse_of_vulnerability", "§4Curse of Vulnerability", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "The wearer takes extra damage from every source.", true),
            new CustomEnchant("curse_of_dullness", "§4Curse of Dullness", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "The weapon deals reduced damage.", true),

            // ==================== PHASE 3 (+20) ====================

            // ---------- WEAPON (+3) ----------
            new CustomEnchant("rampage", "§cRampage", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Each recent kill stacks a temporary damage buff.", false),
            new CustomEnchant("flurry", "§eFlurry", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Chance to instantly land a second, weaker hit.", false),
            new CustomEnchant("chain_lightning", "§bChain Lightning", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Hits arc reduced bonus damage to nearby enemies.", false),

            // ---------- ARMOR (+3) ----------
            new CustomEnchant("bulwark", "§9Bulwark", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Chance to fully negate an incoming melee hit.", false),
            new CustomEnchant("resilience", "§dResilience", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Slowly regenerates Absorption over time.", false),
            new CustomEnchant("steady_footing", "§7Steady Footing", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Reduces knockback taken from hits.", false),

            // ---------- TOOL (+3) ----------
            new CustomEnchant("prospector", "§eProspector", EnchantCategory.TOOL, 1,
                    EnchantDefinitions::isMiningTool,
                    "Sneak + right-click to briefly reveal nearby ores.", false),
            new CustomEnchant("reclaimer", "§5Reclaimer", EnchantCategory.TOOL, 1,
                    EnchantDefinitions::isMiningTool,
                    "Mine spawners as a placeable item instead of destroying them.", false),
            new CustomEnchant("deep_reach", "§6Deep Reach", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Mining speed increases the lower (deeper) you dig.", false),

            // ---------- BOW (+1) ----------
            new CustomEnchant("ricochet", "§bRicochet", EnchantCategory.BOW, 2,
                    EnchantDefinitions::isBow,
                    "Chance for an arrow to bounce to a second nearby target.", false),

            // ---------- SHIELD (3, new category) ----------
            new CustomEnchant("rebound_ward", "§9Rebound Ward", EnchantCategory.SHIELD, 3,
                    EnchantDefinitions::isShield,
                    "Blocking a projectile reflects it back at its source.", false),
            new CustomEnchant("stagger_guard", "§eStagger Guard", EnchantCategory.SHIELD, 3,
                    EnchantDefinitions::isShield,
                    "Blocking a melee hit briefly slows the attacker.", false),
            new CustomEnchant("drainward", "§dDrainward", EnchantCategory.SHIELD, 3,
                    EnchantDefinitions::isShield,
                    "Blocking a hit heals you a small amount.", false),

            // ---------- TRIDENT (3, new category) ----------
            new CustomEnchant("maelstrom", "§bMaelstrom", EnchantCategory.TRIDENT, 3,
                    EnchantDefinitions::isTrident,
                    "A thrown trident's impact knocks back everything nearby.", false),
            new CustomEnchant("tempest_call", "§9Tempest Call", EnchantCategory.TRIDENT, 1,
                    EnchantDefinitions::isTrident,
                    "During a thunderstorm, thrown tridents call down lightning on landing.", false),
            new CustomEnchant("undertow", "§3Undertow", EnchantCategory.TRIDENT, 3,
                    EnchantDefinitions::isTrident,
                    "Melee trident hits pull the target toward you.", false),

            // ---------- SHEARS (2, new category) ----------
            new CustomEnchant("bulk_shear", "§7Bulk Shear", EnchantCategory.SHEARS, 1,
                    EnchantDefinitions::isShears,
                    "Shearing one animal shears every shearable animal nearby too.", false),
            new CustomEnchant("bountiful_shear", "§aBountiful Shear", EnchantCategory.SHEARS, 3,
                    EnchantDefinitions::isShears,
                    "Chance for a shearing action to yield double output.", false),

            // ---------- CURSES (+2) ----------
            new CustomEnchant("curse_of_clumsiness", "§4Curse of Clumsiness", EnchantCategory.SHIELD, 3,
                    EnchantDefinitions::isShield,
                    "Blocking sometimes fails completely, letting full damage through.", true),
            new CustomEnchant("curse_of_the_landlocked", "§4Curse of the Landlocked", EnchantCategory.TRIDENT, 3,
                    EnchantDefinitions::isTrident,
                    "Slows you while holding this trident on dry land.", true),

            // ==================== PHASE 4 (+20) ====================

            // ---------- ELYTRA (4, new category) ----------
            new CustomEnchant("glide_boost", "§bGlide Boost", EnchantCategory.ELYTRA, 3,
                    EnchantDefinitions::isElytra,
                    "Periodic forward speed boost while gliding.", false),
            new CustomEnchant("safe_landing", "§aSafe Landing", EnchantCategory.ELYTRA, 1,
                    EnchantDefinitions::isElytra,
                    "Negates fall/impact damage right after gliding.", false),
            new CustomEnchant("featherlight", "§fFeatherlight", EnchantCategory.ELYTRA, 3,
                    EnchantDefinitions::isElytra,
                    "Elytra loses durability more slowly.", false),
            new CustomEnchant("sky_diver", "§9Sky Diver", EnchantCategory.ELYTRA, 1,
                    EnchantDefinitions::isElytra,
                    "Brief Slow Falling grace period when you start a fall.", false),

            // ---------- MACE (3, new category) ----------
            new CustomEnchant("seismic_slam", "§6Seismic Slam", EnchantCategory.MACE, 3,
                    EnchantDefinitions::isMace,
                    "A smash attack knocks back everything nearby, not just the target.", false),
            new CustomEnchant("aftershock", "§7Aftershock", EnchantCategory.MACE, 3,
                    EnchantDefinitions::isMace,
                    "A smash attack staggers the target afterward.", false),
            new CustomEnchant("windfall", "§eWindfall", EnchantCategory.MACE, 2,
                    EnchantDefinitions::isMace,
                    "Chance for a smash attack to launch the target upward.", false),

            // ---------- WEAPON (+2) ----------
            new CustomEnchant("momentum_strike", "§dMomentum Strike", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Bonus damage that scales with how fast you're moving.", false),
            new CustomEnchant("last_stand", "§4Last Stand", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Bonus damage while you're below 30% health.", false),

            // ---------- ARMOR (+2) ----------
            new CustomEnchant("monster_ward", "§2Monster Ward", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Reduces damage taken specifically from hostile mobs.", false),
            new CustomEnchant("swift_stride", "§bSwift Stride", EnchantCategory.ARMOR, 2,
                    EnchantDefinitions::isArmor,
                    "Small passive speed boost while sprinting.", false),

            // ---------- TOOL (+2) ----------
            new CustomEnchant("quicksilver", "§bQuicksilver", EnchantCategory.TOOL, 2,
                    EnchantDefinitions::isMiningTool,
                    "Speed boost while mining underwater.", false),
            new CustomEnchant("stonecutter_touch", "§7Stonecutter's Touch", EnchantCategory.TOOL, 1,
                    EnchantDefinitions::isMiningTool,
                    "Mining stone-family blocks yields their refined/cut form.", false),

            // ---------- BOW (+1) ----------
            new CustomEnchant("wind_shot", "§fWind Shot", EnchantCategory.BOW, 3,
                    EnchantDefinitions::isBow,
                    "Arrows fly noticeably faster.", false),

            // ---------- SHIELD (+1) ----------
            new CustomEnchant("spike_wall", "§cSpike Wall", EnchantCategory.SHIELD, 3,
                    EnchantDefinitions::isShield,
                    "Blocking a melee hit damages the attacker.", false),

            // ---------- TRIDENT (+1) ----------
            new CustomEnchant("current_rider", "§3Current Rider", EnchantCategory.TRIDENT, 2,
                    EnchantDefinitions::isTrident,
                    "Swim speed boost while holding this trident in water.", false),

            // ---------- FISHING ROD (+1) ----------
            new CustomEnchant("line_saver", "§eLine Saver", EnchantCategory.FISHING_ROD, 1,
                    EnchantDefinitions::isFishingRod,
                    "This rod doesn't lose durability from fishing.", false),

            // ---------- UNIVERSAL (+1) ----------
            new CustomEnchant("reforged", "§dReforged", EnchantCategory.UNIVERSAL, 3,
                    EnchantDefinitions::isAnyItem,
                    "Slowly repairs itself over time.", false),

            // ---------- CURSES (+2) ----------
            new CustomEnchant("curse_of_brittle_wings", "§4Curse of Brittle Wings", EnchantCategory.ELYTRA, 3,
                    EnchantDefinitions::isElytra,
                    "This elytra loses durability much faster.", true),
            new CustomEnchant("curse_of_heavy_hands", "§4Curse of Heavy Hands", EnchantCategory.MACE, 3,
                    EnchantDefinitions::isMace,
                    "This mace deals reduced damage on non-smash hits.", true),

            // ==================== PHASE 5 (+20) ====================

            // ---------- HORSE ARMOR (4, new category) ----------
            new CustomEnchant("war_charger", "§cWar Charger", EnchantCategory.HORSE_ARMOR, 3,
                    EnchantDefinitions::isHorseArmor,
                    "The horse takes reduced damage.", false),
            new CustomEnchant("swift_gallop", "§bSwift Gallop", EnchantCategory.HORSE_ARMOR, 3,
                    EnchantDefinitions::isHorseArmor,
                    "Speed boost for the horse while ridden.", false),
            new CustomEnchant("steady_gait", "§aSteady Gait", EnchantCategory.HORSE_ARMOR, 1,
                    EnchantDefinitions::isHorseArmor,
                    "The horse takes no fall damage.", false),
            new CustomEnchant("regal_bearing", "§dRegal Bearing", EnchantCategory.HORSE_ARMOR, 2,
                    EnchantDefinitions::isHorseArmor,
                    "The horse slowly regenerates health while ridden.", false),

            // ---------- COMPASS (2, new category) ----------
            new CustomEnchant("pathfinder", "§ePathfinder", EnchantCategory.COMPASS, 1,
                    EnchantDefinitions::isCompass,
                    "Points toward the nearest other player.", false),
            new CustomEnchant("homeward", "§bHomeward", EnchantCategory.COMPASS, 1,
                    EnchantDefinitions::isCompass,
                    "Sneak + right-click to point this compass toward your bed.", false),

            // ---------- CROSSBOW (2, exclusive -- not shared with regular bows) ----------
            new CustomEnchant("double_tap", "§6Double Tap", EnchantCategory.CROSSBOW, 3,
                    EnchantDefinitions::isCrossbowOnly,
                    "Chance to not consume the arrow when firing.", false),
            new CustomEnchant("piercing_bolt", "§7Piercing Bolt", EnchantCategory.CROSSBOW, 3,
                    EnchantDefinitions::isCrossbowOnly,
                    "Bonus damage on bolts that also have vanilla Piercing.", false),

            // ---------- WEAPON (+2) ----------
            new CustomEnchant("adrenaline", "§cAdrenaline", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Bonus damage for a few seconds after you take damage.", false),
            new CustomEnchant("armor_breaker", "§7Armor Breaker", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Bonus damage against heavily-armored targets.", false),

            // ---------- ARMOR (+2) ----------
            new CustomEnchant("high_ground", "§eHigh Ground", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Reduces damage from attackers below you.", false),
            new CustomEnchant("unshakeable", "§9Unshakeable", EnchantCategory.ARMOR, 1,
                    EnchantDefinitions::isArmor,
                    "Immune to Slowness and Mining Fatigue.", false),

            // ---------- TOOL (+2) ----------
            new CustomEnchant("efficient_strikes", "§7Efficient Strikes", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "The tool loses durability more slowly.", false),
            new CustomEnchant("surveyor", "§bSurveyor", EnchantCategory.TOOL, 1,
                    EnchantDefinitions::isMiningTool,
                    "Sneak + right-click to check your current depth.", false),

            // ---------- SHIELD (+1) ----------
            new CustomEnchant("last_line", "§dLast Line", EnchantCategory.SHIELD, 2,
                    EnchantDefinitions::isShield,
                    "Small chance to fully block a hit even without actively blocking.", false),

            // ---------- TRIDENT (+1) ----------
            new CustomEnchant("tidecaller", "§3Tidecaller", EnchantCategory.TRIDENT, 1,
                    EnchantDefinitions::isTrident,
                    "A thrown trident leaves a temporary pool of water where it lands.", false),

            // ---------- MACE (+1) ----------
            new CustomEnchant("concussive_blow", "§5Concussive Blow", EnchantCategory.MACE, 3,
                    EnchantDefinitions::isMace,
                    "Non-smash hits slow the target.", false),

            // ---------- UNIVERSAL (+1) ----------
            new CustomEnchant("keepsake", "§dKeepsake", EnchantCategory.UNIVERSAL, 1,
                    EnchantDefinitions::isAnyItem,
                    "Survives death like Soulbound, but only against non-PvP deaths.", false),

            // ---------- CURSES (+2) ----------
            new CustomEnchant("curse_of_the_wanderer", "§4Curse of the Wanderer", EnchantCategory.COMPASS, 2,
                    EnchantDefinitions::isCompass,
                    "This compass points away from the nearest player instead of toward anything useful.", true),
            new CustomEnchant("curse_of_the_pack", "§4Curse of the Pack", EnchantCategory.HORSE_ARMOR, 3,
                    EnchantDefinitions::isHorseArmor,
                    "The horse takes extra damage.", true),

            // ==================== PHASE 6 (+20) ====================

            // ---------- TOTEM OF UNDYING (3, new category) ----------
            new CustomEnchant("lifeline", "§dLifeline", EnchantCategory.TOTEM, 3,
                    EnchantDefinitions::isTotem,
                    "Extends the Regeneration/Absorption a totem save grants.", false),
            new CustomEnchant("echoing_totem", "§5Echoing Totem", EnchantCategory.TOTEM, 3,
                    EnchantDefinitions::isTotem,
                    "Small chance the totem isn't consumed when it saves you.", false),
            new CustomEnchant("guardian_spirit", "§eGuardian Spirit", EnchantCategory.TOTEM, 1,
                    EnchantDefinitions::isTotem,
                    "A totem save also grants brief Fire Resistance and Slow Falling.", false),

            // ---------- SPYGLASS (2, new category) ----------
            new CustomEnchant("keen_sight", "§bKeen Sight", EnchantCategory.SPYGLASS, 1,
                    EnchantDefinitions::isSpyglass,
                    "Night Vision while holding this spyglass.", false),
            new CustomEnchant("mob_sense", "§2Mob Sense", EnchantCategory.SPYGLASS, 2,
                    EnchantDefinitions::isSpyglass,
                    "Nearby hostile mobs glow while holding this spyglass.", false),

            // ---------- WEAPON (+2) ----------
            new CustomEnchant("opening_strike", "§eOpening Strike", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Bonus damage on the first hit against a target you haven't hit recently.", false),
            new CustomEnchant("true_edge", "§7True Edge", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Bonus damage that scales with the target's armor.", false),

            // ---------- ARMOR (+2) ----------
            new CustomEnchant("juggernaut", "§cJuggernaut", EnchantCategory.ARMOR, 3,
                    EnchantDefinitions::isArmor,
                    "Increases your max health while worn.", false),
            new CustomEnchant("warm_heart", "§dWarm Heart", EnchantCategory.ARMOR, 1,
                    EnchantDefinitions::isArmor,
                    "Immune to the Wither effect.", false),

            // ---------- TOOL (+2) ----------
            new CustomEnchant("night_crew", "§9Night Crew", EnchantCategory.TOOL, 2,
                    EnchantDefinitions::isMiningTool,
                    "Haste boost while mining at night.", false),
            new CustomEnchant("salvager", "§aSalvager", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Chance to save a block's drops from Curse of Ruin.", false),

            // ---------- BOW (+1) ----------
            new CustomEnchant("eagle_eye", "§bEagle Eye", EnchantCategory.BOW, 3,
                    EnchantDefinitions::isBow,
                    "Flatter arrow trajectory over long range.", false),

            // ---------- SHIELD (+1) ----------
            new CustomEnchant("fortify", "§9Fortify", EnchantCategory.SHIELD, 3,
                    EnchantDefinitions::isShield,
                    "Extra damage reduction while blocking, on top of the normal block reduction.", false),

            // ---------- TRIDENT (+1) ----------
            new CustomEnchant("riptide_echo", "§3Riptide Echo", EnchantCategory.TRIDENT, 1,
                    EnchantDefinitions::isTrident,
                    "Brief fall-damage protection after a Riptide launch.", false),

            // ---------- MACE (+1) ----------
            new CustomEnchant("relentless", "§6Relentless", EnchantCategory.MACE, 3,
                    EnchantDefinitions::isMace,
                    "Smash-triggering enchants need less fall distance to activate.", false),

            // ---------- FISHING ROD (+1) ----------
            new CustomEnchant("sturdy_hook", "§bSturdy Hook", EnchantCategory.FISHING_ROD, 2,
                    EnchantDefinitions::isFishingRod,
                    "Chance to reroll a junk catch into a fish instead.", false),

            // ---------- UNIVERSAL (+1) ----------
            new CustomEnchant("ageless", "§fAgeless", EnchantCategory.UNIVERSAL, 1,
                    EnchantDefinitions::isAnyItem,
                    "This item can never fully break -- it always keeps 1 durability left.", false),

            // ---------- HORSE ARMOR (+1) ----------
            new CustomEnchant("beast_of_burden", "§7Beast of Burden", EnchantCategory.HORSE_ARMOR, 3,
                    EnchantDefinitions::isHorseArmor,
                    "The horse takes reduced damage from projectiles specifically.", false),

            // ---------- CURSES (+2) ----------
            new CustomEnchant("curse_of_the_lost", "§4Curse of the Lost", EnchantCategory.TOTEM, 3,
                    EnchantDefinitions::isTotem,
                    "Chance the totem simply fails to save you.", true),
            new CustomEnchant("curse_of_dim_sight", "§4Curse of Dim Sight", EnchantCategory.SPYGLASS, 2,
                    EnchantDefinitions::isSpyglass,
                    "This spyglass gives you Blindness instead of anything useful.", true),

            // ==================== PHASE 7 (+20) ====================

            // ---------- CARVED PUMPKIN (3, new category) ----------
            new CustomEnchant("shrouded", "§7Shrouded", EnchantCategory.CARVED_PUMPKIN, 3,
                    EnchantDefinitions::isCarvedPumpkin,
                    "Chance hostile mobs simply don't notice you.", false),
            new CustomEnchant("false_face", "§8False Face", EnchantCategory.CARVED_PUMPKIN, 3,
                    EnchantDefinitions::isCarvedPumpkin,
                    "Mobs far away are less likely to notice you than mobs up close.", false),
            new CustomEnchant("scarecrow", "§6Scarecrow", EnchantCategory.CARVED_PUMPKIN, 1,
                    EnchantDefinitions::isCarvedPumpkin,
                    "Stand still for a few seconds and hostile mobs lose track of you completely.", false),

            // ---------- WEAPON (+2) ----------
            new CustomEnchant("giant_slayer", "§cGiant Slayer", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Bonus damage against targets with more max health than you.", false),
            new CustomEnchant("momentum_breaker", "§bMomentum Breaker", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Bonus damage against airborne targets.", false),

            // ---------- ARMOR (+2) ----------
            new CustomEnchant("iron_will", "§7Iron Will", EnchantCategory.ARMOR, 1,
                    EnchantDefinitions::isArmor,
                    "Immune to the Weakness effect.", false),
            new CustomEnchant("clear_mind", "§bClear Mind", EnchantCategory.ARMOR, 1,
                    EnchantDefinitions::isArmor,
                    "Immune to Nausea.", false),

            // ---------- TOOL (+2) ----------
            new CustomEnchant("keen_edge", "§eKeen Edge", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Mining single blocks yields bonus experience.", false),
            new CustomEnchant("reinforced", "§7Reinforced", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Chance the tool takes no durability damage at all from a use.", false),

            // ---------- BOW (+1) ----------
            new CustomEnchant("focus_shot", "§eFocus Shot", EnchantCategory.BOW, 3,
                    EnchantDefinitions::isBow,
                    "Bonus damage that scales with how fully you drew the bow.", false),

            // ---------- SHIELD (+1) ----------
            new CustomEnchant("aegis_ward", "§9Aegis Ward", EnchantCategory.SHIELD, 3,
                    EnchantDefinitions::isShield,
                    "A brief damage-reduction buff right after you block.", false),

            // ---------- TRIDENT (+1) ----------
            new CustomEnchant("depth_charge", "§3Depth Charge", EnchantCategory.TRIDENT, 3,
                    EnchantDefinitions::isTrident,
                    "Thrown tridents deal bonus damage to targets in water.", false),

            // ---------- MACE (+1) ----------
            new CustomEnchant("warlords_fury", "§cWarlord's Fury", EnchantCategory.MACE, 2,
                    EnchantDefinitions::isMace,
                    "A smash attack grants you brief Strength afterward.", false),

            // ---------- FISHING ROD (+1) ----------
            new CustomEnchant("patient_angler", "§6Patient Angler", EnchantCategory.FISHING_ROD, 3,
                    EnchantDefinitions::isFishingRod,
                    "The longer you wait for a bite, the better the eventual catch.", false),

            // ---------- UNIVERSAL (+1) ----------
            new CustomEnchant("spare_parts", "§7Spare Parts", EnchantCategory.UNIVERSAL, 1,
                    EnchantDefinitions::isAnyItem,
                    "This item survives explosions that would otherwise destroy it on the ground.", false),

            // ---------- HORSE ARMOR (+1) ----------
            new CustomEnchant("iron_shoes", "§6Iron Shoes", EnchantCategory.HORSE_ARMOR, 1,
                    EnchantDefinitions::isHorseArmor,
                    "The horse is immune to fire while ridden.", false),

            // ---------- COMPASS (+1) ----------
            new CustomEnchant("twin_signal", "§eTwin Signal", EnchantCategory.COMPASS, 1,
                    EnchantDefinitions::isCompass,
                    "Right-click (without sneaking) to toggle between pointing at the nearest player and world spawn.", false),

            // ---------- TOTEM (+1) ----------
            new CustomEnchant("final_gift", "§aFinal Gift", EnchantCategory.TOTEM, 1,
                    EnchantDefinitions::isTotem,
                    "A totem save also refills your hunger.", false),

            // ---------- CURSES (+2) ----------
            new CustomEnchant("curse_of_the_scarecrow", "§4Curse of the Scarecrow", EnchantCategory.CARVED_PUMPKIN, 3,
                    EnchantDefinitions::isCarvedPumpkin,
                    "Actively draws nearby hostile mobs toward you instead of hiding you.", true),
            new CustomEnchant("curse_of_the_heavy_shield", "§4Curse of the Heavy Shield", EnchantCategory.SHIELD, 3,
                    EnchantDefinitions::isShield,
                    "Slows you while holding this shield, blocking or not.", true),

            // ==================== PHASE 8 (+20) ====================

            // ---------- FIREWORK ROCKET (4, new category) ----------
            new CustomEnchant("thruster", "§6Thruster", EnchantCategory.FIREWORK_ROCKET, 3,
                    EnchantDefinitions::isFirework,
                    "Extra forward boost when used while gliding.", false),
            new CustomEnchant("safe_burst", "§bSafe Burst", EnchantCategory.FIREWORK_ROCKET, 1,
                    EnchantDefinitions::isFirework,
                    "Grants brief Slow Falling when used while gliding.", false),
            new CustomEnchant("showstopper", "§dShowstopper", EnchantCategory.FIREWORK_ROCKET, 3,
                    EnchantDefinitions::isFirework,
                    "A burst of firework particles when used while gliding.", false),
            new CustomEnchant("curse_of_the_dud", "§4Curse of the Dud", EnchantCategory.FIREWORK_ROCKET, 3,
                    EnchantDefinitions::isFirework,
                    "Saps some of the boost instead of adding to it.", true),

            // ---------- ONE MORE EACH, ACROSS EXISTING CATEGORIES ----------
            new CustomEnchant("vital_strike", "§4Vital Strike", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Chance to weaken the target and deal a bit of bonus damage.", false),
            new CustomEnchant("clean_slate", "§fClean Slate", EnchantCategory.ARMOR, 1,
                    EnchantDefinitions::isArmor,
                    "Clears every negative effect at once -- on a 30-second cooldown.", false),
            new CustomEnchant("bright_ore", "§eBright Ore", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Mining ore leaves a brief glow where it was.", false),
            new CustomEnchant("close_quarters", "§cClose Quarters", EnchantCategory.BOW, 3,
                    EnchantDefinitions::isBow,
                    "Bonus damage on shots that hit at short range.", false),
            new CustomEnchant("twin_bolt", "§7Twin Bolt", EnchantCategory.CROSSBOW, 3,
                    EnchantDefinitions::isCrossbowOnly,
                    "Chance to fire a second bolt alongside the first.", false),
            new CustomEnchant("brine_blessed", "§3Brine Blessed", EnchantCategory.FISHING_ROD, 3,
                    EnchantDefinitions::isFishingRod,
                    "Bonus experience from catches while it's raining.", false),
            new CustomEnchant("unyielding", "§9Unyielding", EnchantCategory.SHIELD, 3,
                    EnchantDefinitions::isShield,
                    "Reduces knockback taken while blocking.", false),
            new CustomEnchant("sea_hunter", "§2Sea Hunter", EnchantCategory.TRIDENT, 3,
                    EnchantDefinitions::isTrident,
                    "Bonus damage against aquatic mobs, thrown or melee.", false),
            new CustomEnchant("sundering_smash", "§5Sundering Smash", EnchantCategory.MACE, 3,
                    EnchantDefinitions::isMace,
                    "A smash attack briefly weakens the target.", false),
            new CustomEnchant("quick_clip", "§aQuick Clip", EnchantCategory.SHEARS, 1,
                    EnchantDefinitions::isShears,
                    "These shears never lose durability.", false),
            new CustomEnchant("reinforced_wings", "§bReinforced Wings", EnchantCategory.ELYTRA, 3,
                    EnchantDefinitions::isElytra,
                    "Chance the elytra takes no durability damage at all.", false),
            new CustomEnchant("sure_hooves", "§6Sure Hooves", EnchantCategory.HORSE_ARMOR, 1,
                    EnchantDefinitions::isHorseArmor,
                    "The horse is immune to cactus and sweet berry bush damage.", false),
            new CustomEnchant("bounty_finder", "§cBounty Finder", EnchantCategory.COMPASS, 1,
                    EnchantDefinitions::isCompass,
                    "Points at the nearest hostile mob instead of a player.", false),
            new CustomEnchant("totemic_ward", "§dTotemic Ward", EnchantCategory.TOTEM, 2,
                    EnchantDefinitions::isTotem,
                    "A totem save grants a few extra seconds of complete damage immunity.", false),
            new CustomEnchant("loot_sense", "§eLoot Sense", EnchantCategory.SPYGLASS, 2,
                    EnchantDefinitions::isSpyglass,
                    "Nearby dropped items glow while holding this spyglass.", false),
            new CustomEnchant("gourd_ward", "§6Gourd Ward", EnchantCategory.CARVED_PUMPKIN, 1,
                    EnchantDefinitions::isCarvedPumpkin,
                    "Piglins stay neutral toward you, same as wearing gold armor.", false),

            // ==================== PHASE 9 (+20) ====================

            // ---------- LEAD (3, new category) ----------
            new CustomEnchant("calming_lead", "§bCalming Lead", EnchantCategory.LEAD, 1,
                    EnchantDefinitions::isLead,
                    "Leashing a hostile mob immediately calms it down.", false),
            new CustomEnchant("swift_lead", "§eSwift Lead", EnchantCategory.LEAD, 3,
                    EnchantDefinitions::isLead,
                    "Whatever you've leashed moves faster.", false),
            new CustomEnchant("curse_of_the_frayed_lead", "§4Curse of the Frayed Lead", EnchantCategory.LEAD, 3,
                    EnchantDefinitions::isLead,
                    "Chance your leashed mount or mob randomly slips free.", true),

            // ---------- ONE MORE EACH, ACROSS EXISTING CATEGORIES ----------
            new CustomEnchant("ambush", "§4Ambush", EnchantCategory.WEAPON, 3,
                    EnchantDefinitions::isSwordOrAxe,
                    "Bonus damage against mobs that haven't noticed you yet.", false),
            new CustomEnchant("buoyant", "§bBuoyant", EnchantCategory.ARMOR, 2,
                    EnchantDefinitions::isArmor,
                    "Resists sinking in water without full water-walking.", false),
            new CustomEnchant("prospectors_charm", "§aProspector's Charm", EnchantCategory.TOOL, 3,
                    EnchantDefinitions::isMiningTool,
                    "Small chance any block mined also yields a bonus emerald.", false),
            new CustomEnchant("volley_call", "§eVolley Call", EnchantCategory.BOW, 3,
                    EnchantDefinitions::isBow,
                    "Killing a mob with this bow has a chance to return an arrow.", false),
            new CustomEnchant("bolt_storm", "§6Bolt Storm", EnchantCategory.CROSSBOW, 2,
                    EnchantDefinitions::isCrossbowOnly,
                    "Killing a mob with this crossbow grants brief Haste.", false),
            new CustomEnchant("deep_diver", "§3Deep Diver", EnchantCategory.FISHING_ROD, 3,
                    EnchantDefinitions::isFishingRod,
                    "Bonus catch experience when fishing in deep water.", false),
            new CustomEnchant("vanguard", "§9Vanguard", EnchantCategory.SHIELD, 3,
                    EnchantDefinitions::isShield,
                    "Blocking a projectile grants brief Absorption.", false),
            new CustomEnchant("stormrider", "§3Stormrider", EnchantCategory.TRIDENT, 2,
                    EnchantDefinitions::isTrident,
                    "Speed boost while holding this trident during a thunderstorm.", false),
            new CustomEnchant("impact_tremor", "§6Impact Tremor", EnchantCategory.MACE, 2,
                    EnchantDefinitions::isMace,
                    "A smash attack reveals nearby hostile mobs.", false),
            new CustomEnchant("cloudburst", "§bCloudburst", EnchantCategory.ELYTRA, 3,
                    EnchantDefinitions::isElytra,
                    "Occasional speed bursts while gliding through rain.", false),
            new CustomEnchant("nimble_steed", "§aNimble Steed", EnchantCategory.HORSE_ARMOR, 3,
                    EnchantDefinitions::isHorseArmor,
                    "Increases the horse's jump height.", false),
            new CustomEnchant("surveyors_eye", "§eSurveyor's Eye", EnchantCategory.COMPASS, 1,
                    EnchantDefinitions::isCompass,
                    "Announces the biome name whenever you enter a new one.", false),
            new CustomEnchant("vengeful_spirit", "§cVengeful Spirit", EnchantCategory.TOTEM, 1,
                    EnchantDefinitions::isTotem,
                    "A totem save also grants brief Strength, not just defensive buffs.", false),
            new CustomEnchant("steady_scope", "§9Steady Scope", EnchantCategory.SPYGLASS, 3,
                    EnchantDefinitions::isSpyglass,
                    "Reduces knockback taken while holding this spyglass.", false),
            new CustomEnchant("night_owl", "§8Night Owl", EnchantCategory.CARVED_PUMPKIN, 1,
                    EnchantDefinitions::isCarvedPumpkin,
                    "Automatic Night Vision while worn, at night.", false),
            new CustomEnchant("encore", "§dEncore", EnchantCategory.FIREWORK_ROCKET, 3,
                    EnchantDefinitions::isFirework,
                    "Chance to not consume the firework when used while gliding.", false),
            new CustomEnchant("everlasting", "§fEverlasting", EnchantCategory.UNIVERSAL, 1,
                    EnchantDefinitions::isAnyItem,
                    "This item, dropped on the ground, never despawns.", false)
    );
}
