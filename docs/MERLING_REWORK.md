# Merling rework

Merling keeps the upstream `origins:merling` ID and Origins Legacy selection
and network flow. These gameplay changes are server-side and use the existing
Origins component to identify the selected origin.

## Behaviour

- Merling's air is restored after the Origins Legacy water-breathing hook, so it
  no longer loses air or takes gills damage on land.
- The existing `origins:swim_speed` power remains the only swim-speed modifier.
- A Merling attack made while the attacker is in water gets the configured
  multiplier after the vanilla attack, critical and enchantment calculation.
- Items in `origins_reimagined:merling_forbidden_food` are rejected before use.
- Damage types in Minecraft's `#minecraft:is_fire` tag are multiplied.
- Nether desiccation is tracked independently per player and resets on leaving
  the Nether, death, respawn, or origin change.

## Configuration

The common/server values are stored in `config/origins_reimagined.properties`:

```properties
merling.underwater_damage_multiplier=1.20
merling.fire_damage_multiplier=1.50
merling.nether_grace_ticks=200
merling.nether_damage_interval_ticks=40
merling.nether_damage=1.0
```

Invalid negative/non-finite values and intervals below one tick fall back to
safe defaults.

## Data resources

The forbidden-food item tag is additive and can be extended by datapacks. The
Nether effect uses `origins_reimagined:merling_desiccation` and is not fire
damage, so Fire Resistance does not cancel it.

The implementation does not modify origin IDs, layers, selection payloads,
player assignment, or other origins.
