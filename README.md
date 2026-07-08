# AutoEMC 1.12.2 Lite

A Forge 1.12.2 lite rewrite of AutoEMC for ProjectE.

## Included

- Automatic scan after overworld/server world load
- `/autoemc rescan` command
- Crafting recipe EMC solving
- Furnace recipe EMC solving
- ProjectE 1.12.2 custom EMC id format: `modid:item|meta`
- Classic Forge `.cfg` config
- Manual item EMC overrides
- Exact OreDictionary value overrides
- OreDictionary material + form multiplier pricing
- Blacklist/skip rules for dangerous items
- High-tier keyword floors for items such as creative, chaotic, draconic, infinity, allthemodium-style items
- Generated-entry tracking so manual ProjectE entries are preserved

## Important config sections

The config is generated at:

```text
config/autoemc.cfg
```

Useful options:

```cfg
B:scanOnWorldLoad=true
I:startupScanDelayTicks=100
B:alwaysRescan=false
```

Manual values:

```cfg
S:manualValues <
    draconicevolution:draconic_core|0=1000000
    draconicevolution:wyvern_core|0=4000000
    avaritia:resource|5=1000000000
>
```

Exact OreDictionary values:

```cfg
S:oreDictionaryValues <
    ingotCopper=128
    oreCopper=256
    blockCopper=1152
    ingotPlatinum=2048
>
```

Material + form pricing:

```cfg
S:oreDictionaryMaterialValues <
    copper=128
    tin=128
    silver=256
    platinum=2048
>

S:oreDictionaryFormMultipliers <
    ingot=1.0
    dust=1.0
    ore=2.0
    nugget=0.111111
    block=9.0
    gear=4.0
    plate=1.0
>
```

Blacklist examples:

```cfg
S:blacklist <
    *:creative_*
    *:spawn_egg|*
    minecraft:command_block|*
>
```

Tier keyword floors:

```cfg
S:tierKeywordValues <
    creative=9000000000000000
    infinity=1000000000000000
    chaotic=1000000000000
    draconic=1000000000
    wyvern=100000000
>
```

## Notes

After AutoEMC writes `config/ProjectE/custom_emc.json`, restart the game/server so ProjectE loads the new values.

This is still a lite 1.12.2 port. It will not perfectly price every modded item, but these layered rules produce much better results than flat fallback alone.
