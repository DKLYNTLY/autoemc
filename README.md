# AutoEMC

Fills in EMC values for any item ProjectE didn't manage to map on its own, for
Forge 1.16.5.

## How it works

1. ProjectE finishes its normal, recipe-driven EMC mapping during world/server
   startup, exactly as it always does - AutoEMC doesn't touch that process.
2. Once the server has fully started, AutoEMC checks whether the set of
   registered items has changed since the last time it ran (tracked in
   `config/autoemc/known_items.json`). If nothing changed, it stops here -
   no scan, no file write, no reload.
3. If items were added or removed (new/updated/removed mods), it walks the
   full item registry, expanding each item into its NBT-distinct variants
   (potions, tipped arrows, enchanted books, etc. - the same technique
   ProjectE's own `/dumpmissingemc` command uses) and asks ProjectE
   (`ProjectEAPI.getEMCProxy().hasValue(stack)`) which of those still have no
   value.
4. For each one missing, it computes a value from rarity / stack size /
   durability and merges it straight into
   `config/ProjectE/custom_emc.json` - the exact file ProjectE's own
   `/setemc` command writes to and its `CustomEMCMapper` reads from. Existing
   entries (anything you or ProjectE already put there) are never
   overwritten.
5. A few ticks later it runs `/reload` once, which is what folds the new
   file contents into ProjectE's real EMC graph (same as the message
   ProjectE prints after you use `/setemc` yourself).

Nothing here touches ProjectE's internal/undocumented mapping classes - it
only uses the public `ProjectEAPI`, ProjectE's own `custom_emc.json` file
format, and (for the final reload) ProjectE's own command, so it should keep
working across ProjectE point releases for 1.16.5.

## Dropping this into your existing MDK

Copy `src/main/java/com/autoemc/mod` and
`src/main/resources/META-INF/mods.toml` (merge, don't overwrite, if you
already have your own) plus `pack.mcmeta` into your workspace's `src/main`.
Rename the package/modid if you like - just keep them consistent.

### Add the ProjectE dependency

ProjectE isn't published on a normal Maven repo, so pick one of:

**Option A - use the jar you already have** (simplest, guarantees you compile
against the exact PE build your pack uses). Put the ProjectE jar at
`libs/ProjectE-1.16.5-PE1.0.2.jar` (match whatever version string your
modpack ships), then:

```gradle
dependencies {
    compileOnly fg.deobf(files('libs/ProjectE-1.16.5-PE1.0.2.jar'))
}
```

Use `compileOnly` (not `implementation`) since ProjectE will already be
present in the modpack at runtime - you don't want to shade/duplicate it.

**Option B - CurseMaven:**

```gradle
repositories {
    maven { url "https://cursemaven.com" }
}

dependencies {
    compileOnly fg.deobf("curse.maven:projecte-238222:<file-id-for-your-version>")
}
```

Look up the exact file id for your ProjectE build on its CurseForge files
page.

### Build

```
./gradlew build
```

The output jar in `build/libs/` goes in your modpack's `mods` folder
alongside ProjectE.

## Tuning

All the numbers live in `config/autoemc-common.toml`, generated the first
time you launch with the mod installed:

- `rarityBaseValues` - base EMC per Minecraft `Rarity` tier (COMMON /
  UNCOMMON / RARE / EPIC).
- `adjustments` - multipliers for non-stackable and low-stack-size items, a
  per-durability-point bonus, and a floor value.
- `filtering` - `ignoredNamespaces` / `ignoredItems` if you want to exclude
  particular mods or items entirely (e.g. mods that intentionally have
  valueless items).
- `nbt.scanNbtVariants` - whether to also cover NBT-distinct variants
  (potions, enchanted books, material-tagged singularities, etc.), on by
  default.
- `nbt.maxNbtVariantsPerItem` - caps how many creative-tab NBT variants one
  item id can add to `custom_emc.json`. Default is 64.
- `nbt.runNbtPassAfterBasicPass` - legacy alias for `scanNbtVariants`; keep
  using `scanNbtVariants` for new configs.
- `nbt.useLiveNbtMaterialResolution` - registers AutoEMC's supported NBT
  material resolvers with ProjectE's mapper/NBT processor hooks so matching
  tagged stacks can resolve when ProjectE asks for their EMC, even if that
  exact NBT variant was not found during the creative-tab scan.
- `nbt.liveNbtResolverSeedValue` - tiny base value used only to make ProjectE
  call the live NBT processor for supported item ids. The processor replaces
  this value for recognized tagged stacks and leaves higher exact/custom
  values alone.
- `general.alwaysRescan` - force a full rescan on every start instead of only
  when the item registry changed. Off by default; mainly useful while you're
  tuning the base values above, so you don't have to add/remove a mod just to
  trigger another pass.

Since this is a heuristic fallback and not a real recipe-derived value, it's
worth spending a few minutes tuning the base values against a couple of
known items in your pack so the numbers feel consistent with the rest of
your EMC economy.

## Notes / limitations

- This only fills items that end up with **zero contribution from every
  ProjectE mapper** - it never overrides a value ProjectE (or another mod)
  legitimately computed, and it never overwrites an existing
  `custom_emc.json` entry.
- Change detection is based on the set of registered item **ids**. A mod
  that adds new NBT-driven variants of an *existing* item id (e.g. a new
  enchantment) without registering any new item ids won't trigger a rescan
  on its own - toggle `alwaysRescan` (or just delete
  `config/autoemc/known_items.json`) if that happens and you want a manual
  refresh.
- NBT scanning only sees variants a mod exposes through `fillItemCategory`
  for creative-tab listings. That works well for mods like Avaritia where
  each singularity is intentionally listed there, but it is not a guarantee
  for every possible crafted NBT combination. AutoEMC also registers supported
  NBT material resolvers as a live ProjectE mapper/NBT processor path, so
  known resolver item ids can still resolve recognized tagged stacks on
  demand. New NBT-backed item families still need a resolver before AutoEMC
  can understand their tag format.
- Runs once per server start, only when needed. If you want to force a
  refresh without changing mods, delete `config/autoemc/known_items.json`
  (or set `alwaysRescan = true`) and restart.
