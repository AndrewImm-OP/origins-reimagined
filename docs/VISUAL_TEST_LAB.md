# Visual Test Lab

The development-only client command is available when `-Dorigins_overhaul.debug=true` is set:

```text
/originsoverhaul debug visual_lab
```

The lab reads `VisualProfileManager`, uses the existing `PlayerPreviewController` and camera, and does not assign or simulate an origin on the real player. It supports profile search, AUTO/CLASSIC/SLIM preview selection, simulated Phantom power/water/swimming/fire states, capability toggles, clean capture mode (`F8`) and report export (`F5`). `visual_world` opens a compact overlay for the real player’s resolved world profile.

The current lab intentionally uses text controls and keyboard shortcuts while the rendering API is being validated. It is not registered in production without the debug JVM property.
