# AutoEMC

**Automatic, safe EMC value generation for ProjectE.**

AutoEMC scans your modpack's item registry and recipes on startup and fills in missing [ProjectE](https://www.curseforge.com/minecraft/mc-mods/projecte) EMC values automatically, so you're not stuck with a Table of Existence full of "unlearnable" items. It's built for large kitchen-sink modpacks where manually pricing every item is unrealistic.

## What it does

*   **Solves EMC from recipes first.** AutoEMC walks your loaded recipes and derives EMC values from known inputs before ever guessing, so pricing stays internally consistent wherever possible.
*   **Falls back intelligently when there's no recipe.** Items with no recipe path are priced using:
    *   Bundled baseline values pulled directly from ProjectE Integration's shipped data (Botania, EnderIO, and dozens of other mods).
    *   Material/tag-based fallback (ore, ingot, dust, block, raw forms, etc.), matched against `forge`, `mekanism`, `create`, and other configurable tag namespaces.
    *   Rarity-based fallback as a last resort, with a reverse-recipe floor so cheap raw materials can't end up worth less than the processed goods made from them.
*   **Understands NBT-tagged items.** A pluggable NBT material resolver framework (with built-in support for things like Avaritia singularities) prices NBT-variant items based on their embedded material, instead of lumping them all into one generic value. This can run both as a pre-scan pass and as a live ProjectE conversion source that resolves on demand.
*   **Protects the economy from itself.** Several safety layers work together:
    *   Creative-only and no-recipe technical items are locked to `1 EMC` so they can never become a cheap source of infinite value if they somehow leak into survival.
    *   A large-drop guard blocks any single-run change that would crash an already-priced item's value by more than a configurable ratio, and logs it to a quarantine report instead of silently applying it.
    *   An ownership arbiter decides, item by item, whether AutoEMC should keep tracking a value or hand it off to ProjectE/a pack author's manual entry — so legitimate overrides are respected but ProjectE simply echoing AutoEMC's own last value can't cause ownership (and the entry) to be deleted.
*   **Batched and adaptive.** Every scan phase runs in small, adaptively-sized chunks across ticks instead of freezing the server on startup, even on packs with thousands of items.
*   **Transparent.** Every generated value is logged with its source (`baseline`, `tag-material`, `rarity-fallback`, `reverse-recipe-floor`, `creative-no-recipe`, etc.), and the mod writes out diagnostic reports each scan:
    *   `generated_entries.json` — everything AutoEMC currently owns
    *   `underpriced_inputs_report.json` — non-owned items that look priced too low relative to what they're used to craft
    *   `quarantined_drops.json` — value changes that were blocked for looking unsafe

## Requirements

*   Minecraft 1.16.5, Forge
*   [ProjectE](https://www.curseforge.com/minecraft/mc-mods/projecte) (required)
*   [ProjectE Integration](https://www.curseforge.com/minecraft/mc-mods/projecte-integration) (required — AutoEMC's bundled baseline data is sourced from it, and it must be present for those integrations to actually function in-game)

## Configuration highlights

All of the following are configurable in `autoemc-common.toml`:

*   `scanNbtVariants` — scan NBT-tagged item variants (on by default)
*   `useNbtMaterialResolvers` / `useLiveNbtMaterialResolution` — enable material-aware NBT pricing, pre-baked and/or live
*   `giveCreativeNoRecipeItemsOneEmc` — toggle the creative-item safety floor (on by default)
*   `largeDropProtectionThreshold` / `maxAllowedDropRatio` — tune the large-drop guard
*   `ownershipSmallDifferenceRatio` — how much difference between AutoEMC's and ProjectE's value counts as "just noise"
*   `tagFallbackNamespaces` — which tag namespaces are checked during material fallback
*   `consistencyWarnMinRatio` / `consistencyWarnMinAbsoluteDelta` — thresholds for the underpriced-input report

## Notes

*   AutoEMC never overwrites a value that's already correctly priced by ProjectE or a pack author unless it's confident the change is safe.
*   If you're auditing a pack's economy, the debug log and JSON reports in `config/autoemc/` are the fastest way to see exactly what was priced, how, and why.
