# Animation architecture

The selection screen uses client-only, frame-independent animation objects in
`ru.origins_overhaul.client.animation`.

- `AnimationClock` reads monotonic `System.nanoTime()` and clamps a frame delta
  to 100 ms after a stall or window switch.
- `AnimatedValue` interpolates a value with an explicit duration and `Easing`.
- `AnimationState` is the screen state machine: `ENTERING`, `IDLE`,
  `SWITCHING_OUT`, `SWITCHING_IN`, `SUBMITTING`, and `LEAVING`.
- `OriginTransitionController` owns `displayedIndex` and `targetIndex`.
  Requests during a transition replace the target; they do not queue a second
  transition. This is the latest-target-wins policy.
- `TextRevealController` owns elapsed reveal time and skip state.
- `AnimatedTextLine` snapshots each formatted code point and its Minecraft
  `Style`. It creates a visible `FormattedCharSequence` without slicing UTF-16
  code units.
- `AnimatedOriginContent` caches wrapped description lines by origin and column
  width. It is rebuilt after screen resize or when a different origin is shown.

The screen updates the clock once per render pass and never uses server ticks or
frame-count increments to determine animation speed. The packet flow remains
unchanged: animations only affect presentation and input timing; selection is
still sent through Origins Legacy's standard packets.

`reduce_motion=true` disables movement, scale, bobbing, acceleration and
typewriter reveal, leaving a short fade. Disabling transitions or text animation
also puts all elements directly into their final visual state.
