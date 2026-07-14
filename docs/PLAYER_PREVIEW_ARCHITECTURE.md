# Player preview architecture

The preview is split into five responsibilities:

- `PlayerAppearanceSnapshot` captures profile, cached `PlayerSkin`, body texture,
  slim/wide model type and outer-layer preferences.
- `PlayerPreviewCamera` owns current/target yaw, pitch, zoom and pan. It applies
  clamped frame-independent damping and auto rotation.
- `PlayerPreviewInputHandler` maps left-button rotation, right-button pan,
  wheel zoom, release and double-click reset into camera operations.
- `PlayerPreviewRenderer` owns the cached wide/slim `PlayerModel` instances and
  submits the vanilla GUI skin render state.
- `PlayerPreviewController` coordinates snapshot refresh, camera update,
  renderer state and `PreviewOriginContext`.

The screen only forwards bounds and input to the controller. It never mutates
the gameplay player. Origin transitions update only `PreviewOriginContext`;
visual profiles can consume that context in a later stage.

Models are rebuilt when the screen is initialized/resource models are available,
not each frame. Appearance refresh is throttled to 500 ms to accommodate a skin
that becomes available after the screen opens without performing a skin lookup in
every render call.
