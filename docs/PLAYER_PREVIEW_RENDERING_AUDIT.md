# Minecraft 26.1.2 player preview rendering audit

Audited against the 26.1.2 deobfuscated client API used by this project.

## Relevant API

- `net.minecraft.client.gui.GuiGraphicsExtractor.skin(PlayerModel, Identifier,
  float scale, float rotationX, float rotationY, float pivotY, int x0, int y0,
  int x1, int y1)` adds a `GuiSkinRenderState` to the GUI render state.
- Mojang's `net.minecraft.client.gui.components.PlayerSkinWidget` uses this
  exact method. Its constructor bakes `ModelLayers.PLAYER` and
  `ModelLayers.PLAYER_SLIM` from the `EntityModelSet`; no entity is created.
- `GuiSkinRenderer` consumes `GuiSkinRenderState`, sets
  `Lighting.Entry.PLAYER_SKIN`, applies X/Y rotations, and renders the cached
  `PlayerModel` with `PlayerModel.renderToBuffer`.
- `net.minecraft.client.player.AbstractClientPlayer.getSkin()` returns the
  cached `net.minecraft.world.entity.player.PlayerSkin`. The skin contains body,
  cape, elytra and `PlayerModelType` (`SLIM` or `WIDE`). Texture identifiers are
  obtained through `ClientAsset.Texture.texturePath()`.
- Skin layer preferences are available through
  `Player.isModelPartEnabled(PlayerModelPart)`. The six outer-layer flags are
  copied into `PlayerAppearanceSnapshot` once, not queried during rendering.

## Selected architecture

The implementation uses the direct GUI skin pipeline (variant C). It caches two
`PlayerModel` instances in `PlayerPreviewRenderer`, selects the wide or slim
model from `PlayerAppearanceSnapshot.modelType()`, and calls
`GuiGraphicsExtractor.skin(...)` inside the preview rectangle's scissor.

No `Player`, `AbstractClientPlayer`, preview entity, entity ID, world list,
gameplay tick, equipment mutation or gameplay render state is used. The real
client player is read only to create an immutable appearance snapshot.

The current pipeline intentionally renders skin and configured outer layers only.
`GuiSkinRenderer` does not render armor, held items or cape geometry, so those
features remain disabled rather than being faked. The body/cape/elytra texture
identifiers are retained in the snapshot for the future renderer integration.

## Rendering isolation

`PlayerPreviewRenderer` enables a GUI scissor only around the layout preview
rectangle and restores it in `finally`. It does not modify global entity
rotation, lighting, shader color, world state or the real player. Lighting is
owned by Mojang's downstream `GuiSkinRenderer` for the submitted GUI render
state.
