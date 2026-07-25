package com.nexusuniverse.enchants;

import com.nexusuniverse.enchants.combat.ArmorDefenseListener;
import com.nexusuniverse.enchants.combat.BleedManager;
import com.nexusuniverse.enchants.combat.DamageTracker;
import com.nexusuniverse.enchants.combat.OpeningStrikeTracker;
import com.nexusuniverse.enchants.combat.RampageManager;
import com.nexusuniverse.enchants.combat.SecondWindManager;
import com.nexusuniverse.enchants.combat.WeaponEnchantListener;
import com.nexusuniverse.enchants.compass.CompassEnchantListener;
import com.nexusuniverse.enchants.core.EnchantAnvilListener;
import com.nexusuniverse.enchants.core.EnchantRegistry;
import com.nexusuniverse.enchants.core.EnchantTomeItems;
import com.nexusuniverse.enchants.core.SoulboundListener;
import com.nexusuniverse.enchants.core.UniversalEnchantListener;
import com.nexusuniverse.enchants.elytra.ElytraEnchantListener;
import com.nexusuniverse.enchants.fishing.FishingEnchantListener;
import com.nexusuniverse.enchants.firework.FireworkEnchantListener;
import com.nexusuniverse.enchants.horse.HorseArmorListener;
import com.nexusuniverse.enchants.lead.LeadEnchantListener;
import com.nexusuniverse.enchants.mace.MaceEnchantListener;
import com.nexusuniverse.enchants.passive.PassiveEffectManager;
import com.nexusuniverse.enchants.projectile.ArrowEnchantListener;
import com.nexusuniverse.enchants.pumpkin.PumpkinEnchantListener;
import com.nexusuniverse.enchants.pumpkin.StillnessTracker;
import com.nexusuniverse.enchants.shears.ShearsEnchantListener;
import com.nexusuniverse.enchants.shield.ShieldEnchantListener;
import com.nexusuniverse.enchants.tools.ToolEnchantListener;
import com.nexusuniverse.enchants.totem.TotemEnchantListener;
import com.nexusuniverse.enchants.trident.TridentEnchantListener;
import com.nexusuniverse.enchants.walk.ElementalWalkAnvilListener;
import com.nexusuniverse.enchants.walk.ElementalWalkItems;
import com.nexusuniverse.enchants.walk.ElementalWalkManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NexusEnchantsPlugin extends JavaPlugin {

    private ElementalWalkItems elementalWalkItems;
    private ElementalWalkManager elementalWalkManager;

    private EnchantRegistry enchantRegistry;
    private EnchantTomeItems enchantTomeItems;
    private BleedManager bleedManager;
    private SecondWindManager secondWindManager;
    private RampageManager rampageManager;
    private DamageTracker damageTracker;
    private OpeningStrikeTracker openingStrikeTracker;
    private PassiveEffectManager passiveEffectManager;
    private HorseArmorListener horseArmorListener;
    private StillnessTracker stillnessTracker;
    private LeadEnchantListener leadEnchantListener;

    private int secondCounter = 0;

    @Override
    public void onEnable() {
        this.elementalWalkItems = new ElementalWalkItems(this);
        this.elementalWalkManager = new ElementalWalkManager(elementalWalkItems);

        this.enchantRegistry = new EnchantRegistry(this);
        this.enchantTomeItems = new EnchantTomeItems(this, enchantRegistry);
        this.bleedManager = new BleedManager();
        this.secondWindManager = new SecondWindManager();
        this.rampageManager = new RampageManager();
        this.damageTracker = new DamageTracker();
        this.openingStrikeTracker = new OpeningStrikeTracker();
        this.passiveEffectManager = new PassiveEffectManager(enchantRegistry);
        this.horseArmorListener = new HorseArmorListener(enchantRegistry);
        this.stillnessTracker = new StillnessTracker();
        this.leadEnchantListener = new LeadEnchantListener(enchantRegistry);

        getCommand("nexusenchants").setExecutor(new NexusEnchantsCommand(this));

        getServer().getPluginManager().registerEvents(new ElementalWalkAnvilListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantAnvilListener(this, enchantRegistry, enchantTomeItems), this);
        getServer().getPluginManager().registerEvents(new WeaponEnchantListener(enchantRegistry, bleedManager, rampageManager, damageTracker, openingStrikeTracker), this);
        getServer().getPluginManager().registerEvents(new ArmorDefenseListener(enchantRegistry, secondWindManager, damageTracker), this);
        getServer().getPluginManager().registerEvents(new ToolEnchantListener(enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new ArrowEnchantListener(enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new FishingEnchantListener(enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new SoulboundListener(enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new UniversalEnchantListener(enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new ShieldEnchantListener(enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new TridentEnchantListener(this, enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new ShearsEnchantListener(enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new ElytraEnchantListener(enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new MaceEnchantListener(enchantRegistry), this);
        getServer().getPluginManager().registerEvents(horseArmorListener, this);
        getServer().getPluginManager().registerEvents(new CompassEnchantListener(enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new TotemEnchantListener(this, enchantRegistry), this);
        getServer().getPluginManager().registerEvents(new PumpkinEnchantListener(enchantRegistry, stillnessTracker), this);
        getServer().getPluginManager().registerEvents(new FireworkEnchantListener(this, enchantRegistry), this);
        getServer().getPluginManager().registerEvents(leadEnchantListener, this);

        // Every tick: the surface-hold correction for Lava/Tide Walker, which
        // needs to run as often as possible to keep its jitter minimal.
        // Every 20th tick (~once/second): bleed damage ticks, the passive
        // continuous player effects (Nightsight, Magnetism, Waterborne,
        // Haste Aura, Pathfinder, etc.), ridden-horse effects (Swift
        // Gallop, Regal Bearing), and the stillness tracker Scarecrow reads
        // from -- none of those need tighter than 1-second resolution.
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                elementalWalkManager.tick(player);
            }

            secondCounter++;
            if (secondCounter >= 20) {
                secondCounter = 0;
                bleedManager.tick();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    passiveEffectManager.tick(player);
                    stillnessTracker.tick(player);
                    leadEnchantListener.tick(player);
                }
                for (var world : Bukkit.getWorlds()) {
                    for (AbstractHorse horse : world.getEntitiesByClass(AbstractHorse.class)) {
                        if (!horse.getPassengers().isEmpty()) {
                            horseArmorListener.tickRiddenHorse(horse);
                        }
                    }
                }
            }
        }, 1L, 1L);

        getLogger().info("NexusEnchants enabled -- Lava Walker, Tide Walker, and "
                + enchantRegistry.all().size() + " custom enchants are live.");
    }

    public ElementalWalkItems getElementalWalkItems() {
        return elementalWalkItems;
    }

    public EnchantRegistry getEnchantRegistry() {
        return enchantRegistry;
    }

    public EnchantTomeItems getEnchantTomeItems() {
        return enchantTomeItems;
    }
}
