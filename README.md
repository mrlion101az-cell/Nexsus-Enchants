# NexusEnchants v0.10.0

Custom "enchantments" for weapons, armor, tools, bows, crossbows,
fishing rods, shields, tridents, shears, elytra, maces, horse armor,
compasses, totems, spyglasses, carved pumpkins, firework rockets, and
leads. Started with Lava Walker / Tide Walker (v0.1.0), then 26
(v0.2.0), 18 more (v0.3.0), 20 more (v0.4.0), 20 more (v0.5.0), 20 more
(v0.6.0), 20 more (v0.7.0), 20 more (v0.8.0), 20 more (v0.9.0), and
this version (phase 9) adds **20 more** across one brand-new category
-- leads -- for **184 total, plus Lava/Tide Walker = 186**. Keeps
growing from here (kept at 0.10.0 rather than 1.0.0 -- see the phase 9
section below for why).

## Why "wave 1" and not "hundreds," honestly

Writing hundreds of genuinely distinct, working mechanics in one pass
isn't something I can do without it turning into padding -- slight
reskins of the same three ideas pretending to be unique enchants. What
actually scales is the *framework*: one registry, one tome-based
anvil-apply flow, one listener per trigger category (weapon hits, armor
defense, passive ticks, tool breaks). Adding enchant #27 means one entry
in `EnchantDefinitions` plus whatever trigger logic it needs in the
matching listener -- the registry, the tome item, and the anvil
combining all just work automatically. That's the part built to get you
to "hundreds" over further rounds, cheaply, rather than trying to fake
it in one giant unreviewable dump today.

## How it works (same idea as Lava/Tide Walker, generalized)

No fake vanilla `Enchantment` registration (still fragile across
Minecraft versions on modern Paper). Instead: every enchant is a
PersistentDataContainer tag + integer level on the item, with the real
enchant glint applied via `ItemMeta#setEnchantmentGlintOverride`. You
get one from an **Enchanted Book tome** (`/nexusenchants give tome
<id> [level]`) combined with a matching item at an anvil -- combining a
tome with an item that already has that enchant at the same level bumps
it up one, same as vanilla's "combine same enchant to level it up"
convention, capped at that enchant's max level.

Run `/nexusenchants list` in-game to see every enchant's id, category,
max level, and description.

## The 26 enchants

**Weapons** (swords + axes), on-hit unless noted:
- **Vampiric** -- heal a % of damage dealt.
- **Inferno** -- longer burn than vanilla Fire Aspect.
- **Frostbite** -- chance to slow the target.
- **Venom** -- chance to poison the target.
- **Executioner** -- bonus damage to targets below 20% health.
- **Bleed** -- damage over time after the hit (5 seconds, ticks once/sec).
- **Soul Reaper** -- bonus damage + small self-heal against undead.
- **Stun** -- chance to briefly freeze the target (Slowness + Mining
  Fatigue, high amplitude).
- **Knockback Wave** -- knocks back everyone near the target, not just
  the target.
- **Thunderstrike** -- chance for a visual lightning strike (sound +
  visual only, no extra damage/fire beyond what the hit already did).

**Armor** (any piece; most check across all 4 slots, whichever has the
highest level wins):
- **Ironhide** -- flat % damage reduction from any source.
- **Featherfall+** -- fall damage fully negated.
- **Guardian** -- reduces projectile damage.
- **Insulation** -- immune to freeze/powder snow damage.
- **Second Wind** -- once per minute, survive a hit that would've
  killed you, with a burst of Regen + Absorption.
- **Nightsight** -- permanent Night Vision while worn.
- **Magnetism** -- pulls nearby dropped items toward you.
- **Waterborne** -- Water Breathing + faster mining while submerged.
- **Thorns+** -- reflects a % of melee damage back at the attacker.

**Tools** (pickaxe/axe/shovel/hoe):
- **Auto Smelt** -- ores and a few other blocks (sand, cobblestone,
  clay) come out already "smelted."
- **Vein Miner** -- mining one ore block mines the whole connected vein.
- **Treecapitator** -- chopping one log chops the whole connected tree.
- **Excavator** -- mining dirt/sand/gravel-type blocks clears a small
  area (3x3 / 5x5 / 7x7 by level) around it.
- **Fortune+** -- chance at a bonus duplicate drop, stacking on top of
  real vanilla Fortune if the tool also has that.
- **Telepathy** -- mined blocks go straight into your inventory instead
  of dropping on the ground.
- **Haste Aura** -- grants Haste while the tool is held.

## The honest limitations

- **Fortune+ doesn't recompute vanilla's real loot table math** -- it
  just rolls a flat chance to duplicate one of whatever already dropped,
  on top of whatever real Fortune (if any) already produced. A true
  "extend Fortune past its vanilla cap" would need re-deriving the loot
  table logic per block type, which block-break event data doesn't
  expose cleanly.
- **Vein Miner and Treecapitator don't respect tool durability loss
  per block** -- breaking a 40-block vein costs the same durability as
  breaking one block right now. Worth fixing if durability balance
  matters to you; flagging it now rather than pretending it's already
  handled.
- **Bleed damage isn't attributed to the attacker** -- it applies as
  plain damage with no damager, so it won't show up as a specific
  player's kill in death messages or PvP kill-tracking. Worth knowing
  if you're tracking kill stats elsewhere.
- **All the "any armor slot counts" enchants (Ironhide, Guardian,
  Second Wind, etc.) don't require a full matching set** -- one piece
  with the enchant is enough, the other three slots can be anything.
  Simpler for v1; a "needs the full set" variant is a small change if
  you want it later.
- **The anvil UI's displayed XP-level cost is meaningless for these
  combinations**, same caveat as Lava/Tide Walker's scrolls -- the
  result item is correct regardless of the number shown.
- **No permission gating** -- still open to everyone by default, same
  flag as every other plugin in this project during testing.
- **Not compiled or tested in-game yet** -- same disclaimer as always.
  This is a genuinely large addition (5 new listener classes, a flood
  fill, an area-mining loop) -- treat the first build and the first
  real PvP/mining session as real debugging, not a formality.

## Build & deploy
Same flow as always:
```
sdk use java 21.0.11-amzn
mvn clean package
```
Output: `target/NexusEnchants-0.10.0.jar` -> upload to `plugins/`.

## First test to run
`/nexusenchants list` to see everything registered, then:
- `/nexusenchants give tome vein_miner 3` + combine with a pickaxe at
  an anvil, then mine into an iron vein -- confirm the whole connected
  vein breaks and drops correctly.
- `/nexusenchants give tome vampiric 2` on a sword, hit a mob, confirm
  you heal.
- `/nexusenchants give tome thorns_plus 1` on any armor piece, let
  something hit you, confirm it takes reflected damage.
- `/nexusenchants give tome second_wind 1`, let your health get low,
  take a hit that should kill you, confirm you survive instead --
  then confirm it doesn't trigger again within a minute.

---

Bring the next batch of ideas whenever -- more weapon effects, armor
set bonuses, tool utility, whatever comes to mind -- and they go into
this same framework the same way.

---

# Wave 2 (v0.3.0): +18 more, three new categories

Inspired by looking at a commercial plugin's (ExcellentEnchants) bundled
class name list -- not its code, not its text, just which *categories*
of enchant a mature plugin in this space covers that we hadn't touched.
Three real gaps closed this round: bows/arrows, fishing rods, and
curses. Everything below is original design and implementation.

One correction made during review: a handful of names in the first
draft of this wave (a bow teleport enchant, a distance-damage bow
enchant, a fishing XP enchant, and all four curse names) matched
ExcellentEnchants' own naming choices exactly, not just the underlying
mechanic idea. Generic descriptive names (Explosive Arrows, Poisoned
Arrows) are fine to share across plugins -- that's just describing what
something does. But "Curse of Mediocrity" or "Seasoned Angler" are
specific creative phrasing, not generic description, so those got
renamed to original names (Blink Shot, Longshot, Angler's Luck, Curse
of Brittleness/Ruin/Vulnerability/Dullness) before this shipped. Same
mechanics, different names, going forward with a clean line kept
between "generic idea" and "someone else's specific wording."

## What's new

**Bow/Arrow (6, new category)** -- these work differently from melee:
the enchant levels get snapshotted onto the arrow itself the moment
it's fired, since the shooter could swap items before the arrow lands.
- **Explosive Arrows** -- explode on impact (doesn't break blocks).
- **Poisoned Arrows** -- poisons on hit.
- **Vampiric Arrows** -- heals the shooter on hit.
- **Confusing Arrows** -- Nausea on hit.
- **Blink Shot** -- teleports you to wherever the arrow lands.
- **Longshot** -- bonus damage that scales with distance traveled.

**Fishing Rod (3, new category):**
- **Double Catch** -- chance to catch two items at once.
- **Angler's Luck** -- chance at bonus XP per catch.
- **Quick Bite** -- shortens the wait-time window before a fish bites,
  via real `FishHook` API (`setMinWaitTime`/`setMaxWaitTime`) -- not a
  fake "auto reel," since actually auto-clicking for the player isn't
  something the plugin API exposes honestly.

**Curses (4, new -- deliberately negative):**
- **Curse of Brittleness** (tool) -- extra durability loss per use.
- **Curse of Ruin** (tool) -- chance for a block's drops to be
  destroyed instead of dropping.
- **Curse of Vulnerability** (armor) -- extra damage taken from everything.
- **Curse of Dullness** (weapon) -- reduced damage dealt.

Curses go on the same way as everything else (tome + anvil) -- there's
no special "can't be removed" logic needed, because grindstones only
strip real vanilla `Enchantment` objects, and none of these are one. A
PDC tag was already outside anything the grindstone touches, so
*every* enchant in this whole plugin has effectively been
grindstone-proof since wave 1 -- that's a side effect of how the whole
system is built, not something added specifically for curses.

**Also added to existing categories:**
- **Cure** (armor) -- periodic chance to strip one negative potion effect.
- **Wisdom** (armor) -- increases XP gained from all sources.
- **Soulbound** (universal -- any item at all) -- survives death instead
  of dropping, returned to your inventory on respawn.
- **Tunnel** (tool) -- drills straight ahead in whatever direction
  you're facing, distinct from Vein Miner (follows connectivity) and
  Excavator (clears an area) -- a third, genuinely different shape.
- **Regrowth** (tool/hoe) -- harvesting a fully-grown crop replants it
  immediately instead of leaving bare farmland.

**Running total: 44 registered enchants + Lava Walker + Tide Walker = 46.**

## New honest limitations from this wave

- **Vein Miner/Treecapitator/Excavator/Tunnel all share the same
  durability gap as before** -- Curse of Brittleness adds *extra*
  durability loss per block, but the base tools still don't lose
  durability per-block on multi-block mining in general (flagged in
  wave 1, still true).
- **Blink Shot can land you somewhere unsafe** -- inside a wall edge,
  over a drop, etc. -- there's no safety check on the teleport
  destination. Worth a "cancel if the landing spot looks dangerous"
  pass if this becomes a griefing/exploit vector (e.g. teleporting
  through walls into a base).
- **Explosive Arrows never break blocks** (hardcoded `false` for the
  block-damage parameter) -- deliberate, to avoid turf/build damage from
  a ranged weapon, but worth knowing it's a design choice, not a
  limitation of what's possible.
- **Curse of Ruin's "destroyed drops" applies per-block, not
  per-item** -- if a single block would drop multiple item stacks, the
  misfortune roll either keeps or destroys all of them together, not
  each stack independently.
- **Soulbound doesn't distinguish PvP death from any other death** --
  it protects the item regardless of how you died. If you want it to
  NOT protect against player-killed deaths specifically (a common
  design choice in some servers to discourage soulbound abuse in PvP),
  that's a small follow-up change.

Same build/deploy flow, same "not tested in-game yet" disclaimer as
always -- this is an even bigger addition than wave 1, so treat the
first real test session accordingly.

---

# Phase 3 (v0.4.0): +20 more, three brand-new gear categories

Three categories that had zero enchants before this: **Shield**,
**Trident**, and **Shears**. Plus a few more added to Weapon, Armor,
Tool, and Bow. **64 registered enchants total, plus Lava/Tide Walker =
66.**

## What's new

**Weapon (+3):**
- **Rampage** -- each kill within an 8-second window stacks a
  temporary damage buff; the streak resets if you go 8s without a kill.
- **Flurry** -- chance to instantly land a second, weaker hit on the
  same swing.
- **Chain Lightning** -- arcs reduced bonus damage to nearby enemies.
  Has a one-level recursion guard so a chained hit can't itself trigger
  another chain -- without it, two enemies standing near each other
  could theoretically bounce damage back and forth indefinitely.

**Armor (+3):**
- **Bulwark** -- chance to fully negate an incoming melee hit.
- **Resilience** -- slowly regenerates Absorption hearts over time.
- **Steady Footing** -- reduces knockback taken, via Paper's
  `EntityKnockbackByEntityEvent` (scales the actual applied knockback
  vector directly, rather than fighting velocity after the fact).

**Tool (+3):**
- **Prospector** -- sneak + right-click to ping nearby ore blocks with
  particles (capped at 40 pings per use so a dense ore cluster doesn't
  flood the client with particles).
- **Reclaimer** -- mining a spawner gives you a placeable spawner item
  (preserving which mob it spawns) instead of just destroying it --
  vanilla doesn't let *any* tool do this, silk touch included.
- **Deep Reach** -- mining speed scales up the deeper below sea level
  you are.

**Bow (+1):**
- **Ricochet** -- chance for a shot to also hit a second nearby target.
  Doesn't literally redirect the physical arrow (it's already consumed
  by the first hit) -- it's a reduced-damage hit applied to a second
  target, the honest equivalent of "found a second mark."

**Shield (3, new category):**
- **Rebound Ward** -- blocking a projectile reflects it back at its
  source.
- **Stagger Guard** -- blocking a melee hit briefly slows the attacker.
- **Drainward** -- blocking a hit heals you a small amount.

All three only trigger while `Player#isBlocking()` is true with a
tagged shield in either hand.

**Trident (3, new category):**
- **Maelstrom** -- a thrown trident's impact knocks back everything
  nearby.
- **Tempest Call** -- during a thunderstorm, a thrown trident calls
  down lightning where it lands. Deliberately different trigger
  condition from vanilla Channeling (which needs the thrower standing
  in rain with open sky) -- this only needs an active storm, no
  positioning requirement.
- **Undertow** -- melee trident hits (not thrown ones) pull the target
  toward you.

**Shears (2, new category):**
- **Bulk Shear** -- shearing one sheep shears every nearby unsheared
  sheep too.
- **Bountiful Shear** -- chance for a shearing action to also drop a
  bonus wool.

**Curses (+2):**
- **Curse of Clumsiness** (shield) -- chance for a block to still let
  bonus damage through.
- **Curse of the Landlocked** (trident) -- slows you while holding this
  trident on dry land.

## New honest limitations from this phase

- **Rebound Ward is unverified.** Reversing a projectile's velocity the
  instant it registers a hit on a blocking shield is the reasonable
  API-level approach, but whether the arrow entity is still in a state
  where that has a visible effect (versus already marked stuck/consumed
  by the time the event fires) is genuinely something only a real test
  can confirm. Flag it if it doesn't visibly do anything.
- **`EntityKnockbackByEntityEvent` (used for Steady Footing) is a
  newer Paper-specific API** -- I'm reasonably confident it exists on
  1.21 but haven't verified it against this exact Paper build. If it
  fails to compile, that's the first place to look.
- **Bulk Shear and Bountiful Shear only work on sheep**, not mushroom
  cows, snow golems, or beehives -- sheep have simple, well-established
  Bukkit API (`isSheared()`/`setSheared()`) to build on reliably; the
  other shearable types would need more uncertain API surface, so they
  weren't attempted this round.
- **Chain Lightning's recursion guard is a single boolean flag on the
  listener instance**, not per-hit or per-player -- during the brief
  window a chain is resolving, a *different* player's completely
  unrelated chain-lightning hit landing in the same tick would also get
  suppressed. Edge case (two players both chain-hitting in the exact
  same tick), not the common case, but worth knowing about.
- **Reclaimer's spawner pickup doesn't have a cooldown or cost beyond
  normal mining** -- if that feels too strong for your server balance
  (free spawner relocation), an XP or durability cost on top is a small
  addition.
- **Tempest Call can be spammed during any thunderstorm** -- no
  cooldown on the lightning call itself beyond the trident's normal
  throw/retrieve cycle. Worth a cooldown if it becomes a grief vector
  (repeatedly calling lightning near builds/players).

Same "not tested in-game yet" disclaimer as every round -- treat the
first real build and the first real combat/mining/shearing session as
genuine debugging, not a formality. This phase leaned on a few less-
common APIs (`EntityKnockbackByEntityEvent`, `BlockStateMeta` for the
spawner item, `Sheep#setSheared`) more than previous phases did, so
there's a slightly higher chance something here needs a small fix on
first compile -- send me whatever `mvn clean package` says and we'll
knock it out the same way as always.

---

# Phase 4 (v0.5.0): +20 more, two more brand-new categories

**Elytra** and **Mace** join Shield/Trident/Shears as categories with
zero enchants before this round. **84 registered enchants total, plus
Lava/Tide Walker = 86.**

## What's new

**Elytra (4, new category):**
- **Glide Boost** -- periodic forward speed boost while actively gliding.
- **Safe Landing** -- negates fall damage, but only within 3 seconds of
  gliding stopping -- a crash-landing safety net, not blanket fall
  immunity (that's what Featherfall+ is already for).
- **Featherlight** -- the elytra loses durability more slowly.
- **Sky Diver** -- brief Slow Falling grace period right as you start a
  fall (not gliding yet).

**Mace (3, new category):** all three only trigger on an actual smash
attack -- detected via the same fall-distance heuristic vanilla's own
mace smash mechanic uses (≥1.5 blocks of fall at the moment of the
hit), since Bukkit doesn't expose a direct "was this a smash" flag.
These are additions on top of a smash, not replacements for what the
real vanilla mace enchants (Density/Wind Burst/Breach) already do.
- **Seismic Slam** -- a smash knocks back everything nearby, not just
  the target.
- **Aftershock** -- a smash staggers the target afterward (Slowness +
  Mining Fatigue).
- **Windfall** -- chance for a smash to launch the target upward.

**Weapon (+2):** **Momentum Strike** (bonus damage scaling with how
fast you're moving), **Last Stand** (bonus damage below 30% health).

**Armor (+2):** **Monster Ward** (damage reduction specifically from
hostile mobs, stacking with Ironhide's general reduction), **Swift
Stride** (passive Speed while sprinting).

**Tool (+2):** **Quicksilver** (Speed boost while mining underwater --
redefined during implementation from an original "reduces Mining
Fatigue" idea that would've needed intercepting potion-effect
application generically, which is riskier API territory than a clean
tick-based Speed grant), **Stonecutter's Touch** (mining stone-family
blocks yields their cut/polished form -- a stylized simplification of
real stonecutter recipes, not an exact match).

**Bow (+1):** **Wind Shot** -- arrows fly faster.

**Shield (+1):** **Spike Wall** -- blocking a melee hit damages the
attacker directly.

**Trident (+1):** **Current Rider** -- swim speed boost while holding
this trident in water.

**Fishing Rod (+1):** **Line Saver** -- this rod doesn't lose
durability from fishing.

**Universal (+1):** **Reforged** -- slowly self-repairs. Scoped to
equipped armor + held items only, not your entire inventory.

**Curses (+2):** **Curse of Brittle Wings** (elytra loses durability
faster), **Curse of Heavy Hands** (mace deals reduced damage on
*non*-smash hits specifically -- it's still a fine smash weapon, just a
bad choice for regular swinging).

## New honest limitations from this phase

- **The mace "was this a smash" check is a fall-distance heuristic**,
  not a real flag from the game. It matches vanilla's own trigger
  condition closely, but it's still an approximation -- if vanilla's
  actual internal logic differs in some edge case (e.g. exact threshold
  tuning), these enchants could fire slightly out of sync with whether
  the game itself considered it a "real" smash.
- **Safe Landing's 3-second window is tracked in memory per player** --
  resets on plugin reload/restart like everything else non-persisted in
  this project, and doesn't survive a player disconnecting mid-window
  (not that it would matter much in practice).
- **Reforged repairs equipped/held items only**, once per second, by a
  flat amount per level -- not proportional to max durability, so it
  repairs a netherite pickaxe and a wooden hoe at the same absolute
  rate. Simple and predictable; if you want it scaled by item tier,
  that's a follow-up.
- **A bug caught during this round's own review, not shipped**: an
  early draft of Monster Ward was written into the wrong event handler
  (`EntityDamageEvent`, which has no `getDamager()` method, instead of
  `EntityDamageByEntityEvent`, which does) -- would have failed to
  compile. Caught and fixed before packaging, mentioning it because
  it's a good example of why "not tested in-game yet" always means
  something real, not just a boilerplate disclaimer.

Same as every round: not compiled or tested live yet. This phase's
riskiest API surface is probably `PlayerToggleGlideEvent` timing
(Elytra) and the fall-distance smash heuristic (Mace) -- those are the
first two things worth watching closely in your first real test.

---

# Phase 5 (v0.6.0): +20 more, two more brand-new categories

**Horse Armor** and **Compass** join the growing list of categories
that had zero enchants before this round. Also splitting **Crossbow**
out as its own exclusive category for the first time -- it still
shares everything in Bow (explosive/poisoned/vampiric arrows, etc. all
still work on crossbows), but now has two enchants only a crossbow can
get. **104 registered enchants total, plus Lava/Tide Walker = 106.**

## What's new

**Horse Armor (4, new category):** read via the real, standard
`AbstractHorse#getInventory().getArmor()` API.
- **War Charger** -- the horse takes reduced damage.
- **Swift Gallop** -- Speed boost for the horse, only while it's
  actually being ridden.
- **Steady Gait** -- the horse takes no fall damage.
- **Regal Bearing** -- slow health regen for the horse, also only
  while ridden.

**Compass (2, new category):**
- **Pathfinder** -- always points at the nearest other player.
- **Homeward** -- sneak + right-click to point it at your bed (or
  world spawn if you don't have one).

**Crossbow (2, exclusive):**
- **Double Tap** -- chance to not consume the arrow when firing.
- **Piercing Bolt** -- bonus damage on bolts that also have real
  vanilla Piercing (checked via `Arrow#getPierceLevel()`).

**Weapon (+2):** **Adrenaline** (bonus damage for a few seconds after
you take damage -- shares a small `DamageTracker` class with
`ArmorDefenseListener`, which is what actually records the "you got
hit" timestamp), **Armor Breaker** (bonus flat damage against targets
wearing 3+ armor pieces).

**Armor (+2):** **High Ground** (reduced damage from attackers below
you), **Unshakeable** (immune to Slowness and Mining Fatigue --
actively strips them the instant they'd apply, not a chance roll).

**Tool (+2):** **Efficient Strikes** (durability loss reduced, the
generic version of what Featherlight already does for elytra
specifically), **Surveyor** (sneak + right-click to check your current
Y-level and depth below sea level).

**Shield (+1):** **Last Line** -- small chance to fully block a hit
*without* actively raising the shield, for when you didn't react in
time.

**Trident (+1):** **Tidecaller** -- a thrown trident leaves a temporary
water pool where it lands (5 seconds, then reverts to whatever was
there before -- not just assumed air).

**Mace (+1):** **Concussive Blow** -- non-smash hits slow the target,
complementing Aftershock (which is smash-only).

**Universal (+1):** **Keepsake** -- same death protection as Soulbound,
but only against non-PvP deaths. Reuses Soulbound's existing drop-and-
return machinery in `SoulboundListener`, just gated by
`event.getEntity().getKiller() == null`.

**Curses (+2):** **Curse of the Wanderer** (compass points *away* from
the nearest player instead of toward anything useful), **Curse of the
Pack** (horse takes extra damage).

## New honest limitations from this phase

- **The horse tick checks every loaded horse in every loaded world,
  every second, just to filter down to ridden ones.** On a server with
  a horse-heavy build (breeding farm, stables), that's more entities
  scanned per second than anything else in this plugin does. Worth
  watching for lag on such a server; a targeted approach (only track
  horses the instant something mounts them, via `EntityMountEvent`)
  would be more efficient if this turns out to matter in practice.
- **Pathfinder and Curse of the Wanderer only search the same world**,
  capped at 200 blocks -- won't find a player across dimensions or far
  away, by design (unlimited-range player tracking felt like a step
  too far without you weighing in on it first).
- **Tidecaller's water pool can be walked through/mined by anyone
  during its 5 seconds**, and if someone manually changes the block
  before the timer's up, the revert will overwrite whatever they
  placed there with the original block, not their new one. Minor edge
  case, not the common case.
- **Armor Breaker's "3+ armor pieces" check counts empty vs non-empty
  slots only** -- it doesn't check armor quality/material, so a target
  in 3 pieces of leather counts the same as 3 pieces of netherite.

Same disclaimer as always -- not compiled or tested live yet. Send me
the build output and whatever looks wrong in-game and we'll fix it the
same way as every round before this one.

---

# Phase 6 (v0.7.0): +20 more, two more brand-new categories

**Totem of Undying** and **Spyglass** join the list of categories that
had zero enchants before this round -- both genuinely have none in
vanilla. **124 registered enchants total, plus Lava/Tide Walker = 126.**

## What's new

**Totem of Undying (3, new category):** all three layer on top of
vanilla's own totem save (which already grants its own brief
Regeneration/Absorption and clears negative effects) rather than
replacing it -- applied a tick after the save resolves, so they don't
fight with vanilla's own handling.
- **Lifeline** -- extends and strengthens the Regeneration/Absorption
  a save grants.
- **Echoing Totem** -- small chance the totem isn't actually consumed.
- **Guardian Spirit** -- a save also grants brief Fire Resistance and
  Slow Falling.

**Spyglass (2, new category):** implemented as passive effects while
simply *holding* the spyglass, not while actively looking through it --
Bukkit doesn't expose a reliable "is this player currently zoomed in"
flag through the plain API, so rather than guess at something
uncertain, these just work whenever the spyglass is in your hand.
- **Keen Sight** -- Night Vision while held.
- **Mob Sense** -- nearby hostile mobs glow while held.

**Weapon (+2):** **Opening Strike** (bonus damage on the first hit
against a target you haven't hit in the last 10 seconds -- rewards
picking new targets over tunnel-visioning one), **True Edge** (bonus
damage that scales with the target's actual Armor attribute value,
different from Armor Breaker's piece-count check).

**Armor (+2):** **Juggernaut** (Health Boost while worn), **Warm
Heart** (immune to the Wither effect).

**Tool (+2):** **Night Crew** (Haste while mining between in-game
13000-23000 ticks, i.e. night), **Salvager** (chance to save a block's
drops specifically from Curse of Ruin's destroy roll -- a direct
counter-play between two existing enchants, not a new standalone effect).

**Bow (+1):** **Eagle Eye** -- a small upward velocity nudge at the
moment of firing, flattening the arrow's trajectory over long range.

**Shield (+1):** **Fortify** -- extra flat damage reduction while
blocking, stacking with vanilla's own block reduction.

**Trident (+1):** **Riptide Echo** -- brief fall-damage immunity after
a Riptide launch, via the real `PlayerRiptideEvent`.

**Mace (+1):** **Relentless** -- lowers the fall-distance threshold
needed for Seismic Slam/Aftershock/Windfall to trigger, making smashes
easier to land.

**Fishing Rod (+1):** **Sturdy Hook** -- chance to reroll a junk catch
(old boots, a stick, rotten flesh, etc.) into an actual fish.

**Universal (+1):** **Ageless** -- the item can never fully break, via
a new dedicated `UniversalEnchantListener` that clamps durability
damage a tick before it would hit zero, kept separate from the
category-specific item-damage handlers (Elytra, Fishing Rod, Tool each
already have their own) since this one deliberately isn't scoped to
any material.

**Horse Armor (+1):** **Beast of Burden** -- reduced damage from
projectiles specifically, stacking with War Charger's general reduction.

**Curses (+2):** **Curse of the Lost** (totem) -- a real risk/reward
curse: chance the totem simply fails, cancelling the save outright, not
just a flavor debuff. **Curse of Dim Sight** (spyglass) -- Blindness
instead of anything useful while held.

## New honest limitations from this phase

- **`PlayerRiptideEvent#getItem()` (used for Riptide Echo) is an API
  I'm reasonably but not 100% confident about** -- same category of
  risk as `EntityKnockbackByEntityEvent` back in phase 4, which turned
  out fine, but flagging it the same way rather than pretending I've
  verified it.
- **Curse of the Lost is a genuinely punishing curse** -- at high
  level and bad luck, it means dying with a totem in hand and it just...
  not working. Intentional (that's the whole point of a curse with
  real teeth), but worth knowing before handing one out, or before
  putting one on a totem you're relying on.
- **Ageless prevents the item from fully breaking, but doesn't restore
  any durability** -- it sits at 1 durability point forever if it keeps
  taking damage, it just never crosses into "broken." Pair it with
  Reforged (from phase 4) if you want it to also slowly heal back up.
- **Salvager only counters Curse of Ruin specifically**, not general
  bad luck on Fortune-style rolls -- it's a direct answer to one other
  enchant in this plugin, not a broad "never fail a drop" effect.

Same as always: not compiled or tested live yet. If I had to guess
where phase 6 is most likely to need a small fix, it's the
`PlayerRiptideEvent` and `EntityResurrectEvent#getHand()` calls --
send me the build output either way and we'll go from there.

---

# Phase 7 (v0.8.0): +20 more, one new category (and why only one)

Went looking for a second brand-new category this round the same way
as every phase before -- Brush and Saddle were the leading candidates.
Both got set aside: Brush's "detect an active brush stroke" isn't
something I could find a confidently-real Bukkit event for, and Saddle
turns out to only store as a real, taggable `ItemStack` on true
`AbstractHorse` mounts (not pigs, striders, or camels, which just use a
boolean flag with no item data at all) -- meaning it would've heavily
overlapped Horse Armor's existing scope while quietly not working on
the other mounts its name implies it should. Forcing either one in
just to hit a "2 new categories" habit would've meant either guessing
at an uncertain API or shipping something narrower than it claims to
be. So: **one** new category this round, and the rest of the batch
goes into real depth on what's already built. **144 registered
enchants total, plus Lava/Tide Walker = 146.**

## What's new

**Carved Pumpkin (3, new category):** all three work by cancelling a
hostile mob's `EntityTargetEvent` -- meaning they can stop a mob from
starting to hunt you, but can't make a mob that's already mid-chase
give up (Bukkit doesn't expose an "un-target" outside this event).
- **Shrouded** -- flat chance a mob simply doesn't notice you.
- **False Face** -- mobs far away are less likely to notice you than
  mobs already close, simulating a shortened detection range.
- **Scarecrow** -- stand still for 3 seconds (tracked once/second, so
  not frame-perfect) and hostile mobs lose track of you completely.

**Weapon (+2):** **Giant Slayer** (bonus damage against targets with
more max health than you), **Momentum Breaker** (bonus damage against
airborne targets).

**Armor (+2):** **Iron Will** (immune to Weakness), **Clear Mind**
(immune to Nausea).

**Tool (+2):** **Keen Edge** (bonus XP from single-block mining --
doesn't apply to Vein Miner/Excavator/Tunnel's own drop handling, since
those cancel the event and vanilla's XP-drop mechanism never runs for
them either), **Reinforced** (chance to take zero durability damage
from a use at all).

**Bow (+1):** **Focus Shot** -- bonus damage scaling with
`EntityShootBowEvent#getForce()`, i.e. how fully you drew the bow
before releasing. Applied directly to the arrow's own damage value at
launch, not tagged for later.

**Shield (+1):** **Aegis Ward** -- a brief Resistance buff right after
a successful block.

**Trident (+1):** **Depth Charge** -- bonus damage on thrown-trident
hits against targets in water.

**Mace (+1):** **Warlord's Fury** -- a smash attack grants you brief
Strength afterward.

**Fishing Rod (+1):** **Patient Angler** -- the longer you've been
waiting for a bite, the better the eventual catch's bonus-XP odds
(caps out around a minute of waiting).

**Universal (+1):** **Spare Parts** -- an item on the ground survives
an explosion that would otherwise destroy it.

**Horse Armor (+1):** **Iron Shoes** -- Fire Resistance for the horse
while ridden.

**Compass (+1):** **Twin Signal** -- right-click *without* sneaking
(sneaking is Homeward's trigger) to toggle between pointing at the
nearest player and world spawn.

**Totem (+1):** **Final Gift** -- a totem save also refills your hunger.

**Curses (+2):** **Curse of the Scarecrow** (pumpkin) -- actively pulls
nearby hostile mobs' attention toward you instead of hiding you, the
inverse of everything else in this category. **Curse of the Heavy
Shield** -- slows you while holding this shield, whether you're
blocking or not.

## New honest limitations from this phase

- **Scarecrow/stillness detection runs once per second, not
  continuously** -- "stood still for 3 seconds" really means "the last
  3 once-per-second checks all landed within a small movement
  tolerance," which is close enough for the mechanic's purpose but
  isn't frame-perfect.
- **Curse of the Scarecrow only pulls mobs that don't already have a
  target** (`mob.getTarget() == null`) -- it won't redirect a mob
  that's already chasing someone else toward you instead.
- **Patient Angler's bonus is XP only, not a better item catch** --
  "better catch" here means bonus experience orbs, not influencing
  what item you actually reel in.
- **Keen Edge is real but narrower than it might sound** -- it only
  fires on the plain single-block path. If you're also running Vein
  Miner or Excavator on the same tool, those bypass it entirely for
  the blocks they handle in bulk.

Same as always: not compiled or tested live yet. `EntityTargetEvent`
(Carved Pumpkin) and `EntityShootBowEvent#getForce()` (Focus Shot) are
both APIs I'm confident about; if anything in this phase needs a fix,
my guess is it'll be somewhere in the pumpkin/stillness interaction
rather than those two. Send me the build output either way.

---

# Phase 8 (v0.9.0): +20 more, one new category, and one of everything else

**Firework Rocket** is the new category this round -- tied to using a
firework while gliding, the same real condition vanilla's own
elytra-boost mechanic requires. Rather than pile more depth onto a
handful of categories, the other 16 enchants this round are spread one
each across every existing category that didn't already get something
in phase 8's firework batch -- so nothing gets left behind as the
plugin grows. **164 registered enchants total, plus Lava/Tide Walker =
166.**

## What's new

**Firework Rocket (4, new category):** all four only trigger when used
while actually gliding.
- **Thruster** -- extra forward velocity on top of vanilla's own boost.
- **Safe Burst** -- brief Slow Falling alongside the boost.
- **Showstopper** -- a burst of firework particles at the moment of use.
- **Curse of the Dud** -- saps some of the boost's upward velocity
  instead of adding to it.

One new wrinkle worth naming: fireworks are the first **stackable**
item this plugin has enchanted. Everything before this (armor, tools,
weapons, etc.) maxes at a stack of 1 anyway, so this never came up. A
tagged stack behaves exactly like a tagged single item as far as this
system's concerned -- the whole stack shares one set of PDC tags --
which is correct and expected, just worth flagging as a first.

**One addition each, across every other category:**
- **Weapon** -- **Vital Strike**: chance to weaken the target plus a
  small bonus-damage kicker.
- **Armor** -- **Clean Slate**: clears every negative effect at once,
  on a 30-second cooldown (only starts the cooldown if it actually had
  something to clean).
- **Tool** -- **Bright Ore**: mining ore leaves a brief glow -- and
  unlike Keen Edge (phase 7), this one lives inside `breakOneBlock()`
  itself, so it applies uniformly across the single-block, Vein Miner,
  Excavator, and Tunnel paths alike, not just the plain single-block case.
- **Bow** -- **Close Quarters**: bonus damage on shots that hit within
  5 blocks of where they were fired.
- **Crossbow** -- **Twin Bolt**: chance to fire a second bolt alongside
  the first.
- **Fishing Rod** -- **Brine Blessed**: bonus catch XP while it's raining.
- **Shield** -- **Unyielding**: reduces knockback taken while blocking,
  via the same `EntityKnockbackByEntityEvent` pattern as Steady Footing.
- **Trident** -- **Sea Hunter**: bonus damage against aquatic mobs
  (Guardians, Drowned, fish, Dolphins, Turtles, Axolotls), works both
  thrown and melee.
- **Mace** -- **Sundering Smash**: a smash attack briefly weakens the target.
- **Shears** -- **Quick Clip**: never lose durability.
- **Elytra** -- **Reinforced Wings**: chance to take zero durability
  damage, the elytra-specific parallel to Tool's Reinforced (phase 7).
- **Horse Armor** -- **Sure Hooves**: immune to cactus and sweet berry
  bush damage.
- **Compass** -- **Bounty Finder**: points at the nearest hostile mob
  instead of a player -- the PvE mirror of Pathfinder.
- **Totem** -- **Totemic Ward**: a save also grants a few extra seconds
  of complete damage immunity, on top of vanilla's own brief
  invulnerability window.
- **Spyglass** -- **Loot Sense**: nearby dropped items glow while held.
- **Carved Pumpkin** -- **Gourd Ward**: Piglins stay neutral toward
  you, the same effect gold armor already gives, just via the pumpkin.

## New honest limitations from this phase

- **Loot Sense never un-glows an item.** There's no cheap "have I
  already marked this one" check without extra bookkeeping this round
  didn't add, so an item can stay glowing after you walk away or put
  the spyglass down. Cosmetic side effect only, not a gameplay bug.
- **Firework enchants apply their effect even if the player is about
  to run out of fireworks** -- there's no "last one in the stack"
  special case, it's the same trigger every time regardless of
  remaining stack size.
- **Sea Hunter's aquatic-mob list is a fixed set I chose**
  (`Guardian, Elder Guardian, Drowned, Cod, Salmon, Pufferfish,
  Tropical Fish, Squid, Glow Squid, Dolphin, Turtle, Axolotl`) -- not
  derived from any official "aquatic" tag in the API, so if a mob feels
  like it should count and doesn't, that's a one-line fix, not a deep
  redesign.
- **Totemic Ward's damage immunity is absolute, not reduced** -- for
  its duration, the player takes zero damage from anything, not just
  reduced damage. Intentional given the enchant's description, but
  worth knowing it's an all-or-nothing window, not a resistance buff.

Same as always: not compiled or tested live yet. Send me the build
output and whatever looks off in-game.

---

# Phase 9 (v0.10.0): +20 more, one new category (Lead)

**Lead** is the new category this round -- leashing mechanics have
been completely untouched until now. Kept the version at 0.10.0
rather than jumping to 1.0.0 even though this is technically the 10th
round of building: 1.0.0 usually signals "stable, ready for
production," and this still hasn't been through a single real compile
or in-game test, so that signal would be misleading no matter how much
code has accumulated. **184 registered enchants total, plus Lava/Tide
Walker = 186.**

## What's new

**Lead (3, new category):** the real design challenge here is that
leashing *consumes* the lead item into an invisible connection --
once an entity is leashed, there's no `ItemStack` left to read enchant
tags off of. So Swift Lead and Curse of the Frayed Lead snapshot their
level into a small map at the moment of leashing
(`PlayerLeashEntityEvent`), keyed by the leashed entity's UUID, read
back from that map during the once-per-second tick, and cleared on
`PlayerUnleashEntityEvent`. Calming Lead is the one exception -- it
only needs to fire once, at the instant of leashing, so it doesn't
need the map at all.
- **Calming Lead** -- leashing a hostile mob immediately clears its
  target.
- **Swift Lead** -- whatever's leashed moves faster.
- **Curse of the Frayed Lead** -- chance your leashed mount/mob
  randomly slips free.

**One addition each, across most existing categories:**
- **Weapon** -- **Ambush**: bonus damage against mobs that haven't
  noticed you (`Mob#getTarget() == null`). Worth flagging: if vanilla's
  own AI reacts to a hit by setting its target to the attacker *before*
  this listener runs, Ambush could end up rarely triggering in
  practice -- that's a real timing question only a live test can answer.
- **Armor** -- **Buoyant**: resists sinking in water without full
  water-walking (nudges vertical velocity, doesn't override it).
- **Tool** -- **Prospector's Charm**: small chance any block mined
  also yields a bonus emerald. Lives in `breakOneBlock()` like Bright
  Ore (phase 8), so it applies uniformly across all the mining paths.
- **Bow** -- **Volley Call**: killing a mob has a chance to return an arrow.
- **Crossbow** -- **Bolt Storm**: killing a mob grants brief Haste.
- **Fishing Rod** -- **Deep Diver**: bonus catch XP when fishing below Y=50.
- **Shield** -- **Vanguard**: blocking a projectile grants brief Absorption.
- **Trident** -- **Stormrider**: Speed while holding the trident during
  a thunderstorm.
- **Mace** -- **Impact Tremor**: a smash attack reveals (Glowing)
  nearby hostile mobs.
- **Elytra** -- **Cloudburst**: occasional speed bursts gliding through rain.
- **Horse Armor** -- **Nimble Steed**: increases jump height, via the
  real `AbstractHorse#setJumpStrength()`.
- **Compass** -- **Surveyor's Eye**: announces the biome name whenever
  you enter a new one.
- **Totem** -- **Vengeful Spirit**: a save also grants brief Strength,
  not just defensive buffs.
- **Spyglass** -- **Steady Scope**: reduces knockback taken while held
  -- added into the same `EntityKnockbackByEntityEvent` handler Steady
  Footing (armor) already uses, so the two stack correctly rather than
  fighting over the event.
- **Carved Pumpkin** -- **Night Owl**: automatic Night Vision at night, worn.
- **Firework Rocket** -- **Encore**: chance to not consume the firework
  when used while gliding.
- **Universal** -- **Everlasting**: a dropped copy of this item never
  despawns, via the real `ItemDespawnEvent`.

(Shears didn't get an addition this round, same as phase 7 -- not
every category needs to grow every single time.)

## New honest limitations from this phase

- **Ambush's exact trigger window is genuinely uncertain.** See above
  -- whether `getTarget()` is still null by the time our listener sees
  the hit depends on mob AI timing this environment can't verify without
  a live server.
- **Swift Lead and the Frayed Lead curse only work on entities you're
  actively standing within 12 blocks of** (the tick scans
  `player.getNearbyEntities(12,12,12)` rather than tracking leashed
  entities globally) -- a leashed mob that somehow ends up farther than
  that from its holder (shouldn't normally happen, since leads break
  around that range anyway) would stop getting these effects until back
  in range.
- **Nimble Steed's jump-strength ceiling (2.0) is a value I'm
  reasonably, not fully, confident is the right safe upper bound for
  `setJumpStrength()`** -- if it turns out vanilla clamps or behaves
  oddly above its normal ~1.0 max, that's a one-line adjustment.
- **Steady Scope and Steady Footing both modify the same
  `EntityKnockbackByEntityEvent`, applied sequentially** -- correct and
  intentional (multiplicative stacking), but worth knowing they're not
  independent caps; a very high level of both together could reduce
  knockback quite aggressively.

Same as always: not compiled or tested live yet. Send me the build
output and whatever looks off in-game, especially around leashing and
Ambush's timing.
