# Visual rendering pipeline audit

Minecraft 26.1.2 exposes the GUI skin path through `GuiGraphicsExtractor.skin(...)`, which delegates to `GuiSkinRenderer` and `PlayerModel.renderToBuffer`. It supports the wide/slim baked player models, skin texture, outer skin parts and GUI clipping, but it does not expose a general attachment or armor pass.

Origins Overhaul therefore uses two backends with one shared profile/resolver contract:

- preview: the existing `GuiGraphicsExtractor.skin` path, with a client mixin for the shared model tint pass;
- world: a client-only player render layer attached to `AvatarRenderer`, plus the model-tint hook in `LivingEntityRenderer`.

Both backends consume `PlayerVisualContext`, `ResolvedVisualProfile` and `VisualModifier`. No PNG is edited and no preview entity is inserted into a world. The current world texture overlay is a cutout pass; emissive modifiers currently use the same safe cutout fallback because a separate fullbright vertex path would require a version-specific shader contract. Alpha, geometry and particles remain explicit follow-up backend work.

The GUI path is isolated in client source and client mixins, so dedicated servers never load it.
