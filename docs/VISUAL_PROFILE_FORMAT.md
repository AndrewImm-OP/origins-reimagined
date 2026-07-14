# Visual profile format

Profiles are client resources at `assets/<namespace>/origins_overhaul/visual_profiles/*.json`.

```json
{
  "id": "origins_overhaul:phantom",
  "origin": "origins:phantom",
  "priority": 0,
  "modifiers": [
    {
      "type": "origins_overhaul:model_tint",
      "color": "#E8FFFF",
      "strength": 0.08,
      "condition": {"type": "origins_overhaul:power_active", "power": "origins:phantomize"}
    }
  ],
  "preview_states": [
    {"id":"normal", "name":{"translate":"origins_overhaul.preview.normal"}}
  ]
}
```

`id` identifies the profile and `origin` identifies the Origins origin it decorates. The presentation profile points to `id`. Higher `priority` wins when resource packs provide profiles for the same origin. Invalid modifiers are skipped with a warning; the base skin remains available.

Attachments may use `geometry_type` (`cuboid`, `plane`, `cross_planes`, `segmented_chain`), `size`, `uv`, `anchor`, `offset`, `rotation`, `scale`, `mirror`, armor hide flags and an optional animation object. Particle modifiers use `particle`, `rate`, `radius` and `height`.
