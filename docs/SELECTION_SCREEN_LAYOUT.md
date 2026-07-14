# Selection screen layout

`OriginSelectionLayout.calculate(width, height)` is a pure layout calculator.
It returns bounds for the header, advantage/disadvantage columns, empty preview,
neutral section, navigation, confirmation and list controls. Coordinates are
computed from the current window size and are recalculated by `Screen.init()`.

The renderer does not use the upstream dirt texture. It first renders the
transparent world background and applies a configurable black overlay. The
central preview rectangle is intentionally empty until the preview stage.

Each power column owns a separate scroll offset. The current implementation
wraps descriptions during screen rebuild/render preparation and only draws
lines intersecting the column bounds. The origin list overlay filters prepared
catalog presentations by display name, full ID and namespace.

Configuration keys:

```properties
cinematic_selection_screen=true
selection_background_opacity=0.70
show_neutral_features=true
show_origin_namespace=false
origin_list_threshold=12
```

The animation cache is separate from this layout calculation. It is invalidated
when the screen is resized and keyed by origin ID plus current text width.
