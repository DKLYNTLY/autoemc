# AutoEMC 1.12.2 Lite - Changelog

## 1.12.2 Lite Release Candidate Notes

Status: Beta / Release Candidate, not final stable yet.

This build is a rewrite for Minecraft Forge 1.12.2, not a direct mechanical port from the newer AutoEMC codebase. The 1.12.2 API does not support modern systems such as Brigadier commands, ForgeConfigSpec, Forge item tags, DataComponents, or Smithing Table recipes.

---

## v1.0.0-lite-initial

### Added
- Forge 1.12.2 project structure.
- Classic `mcmod.info` metadata support.
- Old-style Forge `.cfg` configuration.
- `/autoemc rescan` command using 1.12.2 `CommandBase`.
- ProjectE `custom_emc.json` writer.
- 1.12.2 ProjectE item id format:
  - `modid:item|meta`
- Crafting recipe EMC solving.
- Furnace recipe EMC solving.
- Flat fallback EMC for unsolved items.
- OreDictionary fallback support.
- AutoEMC-generated entry tracking:
  - `config/autoemc/generated_entries.json`
- Dedicated debug log:
  - `config/autoemc/autoemc-debug.log`

### Notes
- This version was intentionally built as a lightweight test version.
- Smithing, modern Forge tags, NBT/DataComponents, and Brigadier were not included because they are not 1.12.2 systems.

---

## v1.0.0-lite-info-fixed

### Fixed
- Replaced barebones `mcmod.info` with a proper 1.12.2 metadata file.
- Added better mod description, credits, authorship, Minecraft version, Forge dependency, and ProjectE load-after dependency.
- Rebuilt the zip to preserve the full source tree.

---

## v1.0.0-lite-worldload-fixed

### Added
- Automatic scan on server/world load.
- `WorldEvent.Load` hook.
- Delayed startup scan on server tick so recipes, ProjectE, and OreDictionary have time to initialize.

### Added config
```cfg
B:scanOnWorldLoad=true
I:startupScanDelayTicks=100
```

### Changed
- Server-start event now queues the scan instead of running immediately.

### Behavior
- First server/world load scans automatically.
- If item registry is unchanged, AutoEMC skips the scan.
- `/autoemc rescan` still forces a manual scan.
- `alwaysRescan=true` can force scanning every launch.

---

## v1.0.0-lite-better-emc

### Added
- Manual item EMC overrides.
- Exact OreDictionary EMC values.
- OreDictionary material baseline values.
- OreDictionary form multipliers.
- Blacklist support.
- Tier keyword value floors.

### Added config examples
```cfg
S:manualValues <
    draconicevolution:draconic_core|0=1000000
    draconicevolution:wyvern_core|0=4000000
>
```

```cfg
S:oreDictionaryMaterialValues <
    copper=128
    tin=128
    silver=256
    platinum=2048
>
```

```cfg
S:oreDictionaryFormMultipliers <
    ingot=1.0
    dust=1.0
    ore=2.0
    nugget=0.111111
    block=9.0
    gear=4.0
>
```

```cfg
S:tierKeywordValues <
    creative=9000000000000000
    infinity=1000000000000000
    chaotic=1000000000000
    draconic=1000000000
    wyvern=100000000
>
```

### Changed
Pricing order became:

1. Skip blacklisted items.
2. Use manual item value if configured.
3. Preserve existing ProjectE/manual values.
4. Solve crafting/furnace recipes.
5. Apply tier keyword floor.
6. Use exact OreDictionary value.
7. Use OreDictionary material × form multiplier.
8. Use old simple OreDictionary fallback.
9. Use rarity/stack/durability fallback.

---

## v1.0.0-lite-release-safety

### Added
- Stronger ProjectE value protection.
- Default ProjectE namespace blacklist.
- Dry-run mode.
- Scan report generation.
- Improved odd-name material parsing.

### Added config
```cfg
B:neverOverwriteProjectEValues=true
B:dryRun=false
B:writeReport=true
I:reportLimit=500
```

### Added default blacklist
```cfg
S:blacklist <
    projecte:*
>
```

### Changed
- If ProjectE already reports EMC greater than 0, AutoEMC will not generate, lower, raise, or replace that value.
- ProjectE-owned items are blocked from AutoEMC generation by default.
- Improved fallback parsing for names like:
  - `highyieldgoldore`
  - `highyieldplatore`
  - `highyieldcaluminumore`
  - `platinumoremoreore`
  - `netherite_ingot`
  - `netherite_scrap`
  - `ancient_debris`

### Added material aliases
- `plat` -> `platinum`
- `caluminum` -> `aluminum`
- `aluminium` -> `aluminum`

### Added better defaults
```cfg
ingotNetherite=8192
scrapNetherite=2048
oreNetherite=4096
blockNetherite=73728
ancientDebris=4096
netherite=8192
```

### Added report output
```text
config/autoemc/autoemc-report.txt
```

The report includes:
- Write/dry-run mode.
- Changed entry count.
- Recipe-solved count.
- Fallback-solved count.
- ProjectE-protected count.
- Generated candidate values.

---

## v1.0.0-lite-recipe-floor

### Added
- Recipe output floor enforcement.

### Added config
```cfg
B:enforceRecipeOutputFloor=true
I:maxRecipeFloorPasses=10
```

### Behavior
After fallback assigns values, AutoEMC checks recipes again.

If an AutoEMC-generated recipe output is cheaper than the total EMC of its ingredients, AutoEMC raises the output value.

Example:
```text
9 ingots = 81 EMC total
block currently = 3 EMC
AutoEMC raises block to at least 81 EMC
```

### Safety rules
- Does not change ProjectE-protected values.
- Does not change manualValues.
- Does not lower any value.
- Skips self-referential recipes.
- Runs multiple passes so raised components can raise downstream outputs.
- Report includes:
```text
Recipe floors raised: X
```

### Test observation
The latest scan showed:
- 860 item stacks scanned.
- 816 usable crafting/furnace recipes loaded.
- 21 values solved from recipes.
- 148 values solved from fallback.
- 40 values raised by recipe-floor passes.
- 678 ProjectE-protected values.
- 169 entries written/updated.
- Scan completed in 33 ms.

---

## Current release status

### Ready for
- Closed beta.
- Public beta with warning.
- Modpack testing.
- User feedback on generated EMC balance.

### Not yet ideal for stable release
One important cleanup behavior is still needed.

### Remaining release blocker
When an item becomes blacklisted or ProjectE-protected after a previous AutoEMC run, AutoEMC should remove its stale AutoEMC-generated entry from:

```text
config/ProjectE/custom_emc.json
config/autoemc/generated_entries.json
```

This is needed because old bad entries can remain after the safety rules are added.

Examples seen in existing generated output:
```text
projecte:item.pe_lens_explosive|0 = 8
projecte:item.pe_randomizer|0 = 8
minecraft:barrier|0 = 8
minecraft:bedrock|0 = 8
minecraft:structure_void|0 = 8
minecraft:end_portal_frame|0 = 8
minecraft:mob_spawner|0 = 8
```

### Recommended next version
`v1.0.0-lite-stale-entry-cleanup`

Required changes:
- Remove AutoEMC-owned entries that now match the blacklist.
- Remove AutoEMC-owned entries that ProjectE now protects.
- Remove AutoEMC-owned entries for ProjectE namespace by default.
- Log all removals in `autoemc-report.txt`.
- Keep user/manual entries untouched.

---

## Suggested public beta warning

AutoEMC 1.12.2 Lite is experimental. Back up `config/ProjectE/custom_emc.json` before use. This mod generates EMC values automatically from recipes, furnace recipes, OreDictionary data, and fallback rules. Some values may require pack-author tuning through the config.
