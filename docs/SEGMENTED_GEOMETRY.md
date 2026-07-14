# Segmented geometry

`geometry_type: "segmented_chain"` is baked as one cached `ModelPart` per segment. The parser clamps the segment count to 1–16. Each segment inherits the anchor transform, receives the configured segment offset and bend, and is submitted independently. A sinusoidal phase offset is used for idle sway; reduced motion keeps the static chain shape.

Feline uses six segments. The current segment mesh is cuboid-based per segment; it is no longer a single cuboid fallback, but specialized plane topology remains a future refinement.
