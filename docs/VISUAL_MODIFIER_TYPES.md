# Visual modifier types

The loader currently understands `texture_overlay`, `emissive_overlay`, `model_tint`, `model_alpha`, `geometry_attachment` and `particle_aura`. Conditions include `always`, `preview`, `world`, `local_player`, `power_active`, `power_inactive`, `in_water`, `underwater`, `on_fire`, `sneaking`, `swimming` and `fall_flying`.

`model_tint`, texture overlays, emissive overlays and baked geometry attachments are implemented in preview and world rendering. World particle emission is client-only and rate-limited. Preview particles and whole-model alpha remain pending. Unknown types are ignored without disabling the profile.

Modifiers are ordered by `render_phase`, while preserving JSON order inside a phase. Supported phases are `BEFORE_BODY`, `AFTER_BODY`, `AFTER_OUTER_LAYER` and `EMISSIVE`.
