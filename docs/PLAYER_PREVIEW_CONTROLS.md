# Player preview controls

- Hold left mouse button inside the preview to rotate.
- Hold right mouse button inside the preview to pan.
- Scroll inside the preview to zoom.
- Double-click inside the preview or press `R` to reset yaw, pitch, zoom and
  pan. Reset re-enables auto rotation.
- Auto rotation runs only before the first manual interaction and is disabled by
  `reduce_motion=true`.

Input priority is: search overlay, active preview drag, preview rectangle,
columns, navigation/buttons, then global origin switching. Consequently preview
scroll never changes origin and preview drags never activate a button underneath.
