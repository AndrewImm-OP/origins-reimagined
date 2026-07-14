# Text reveal behavior

Each wrapped description is represented by `AnimatedTextLine`. The line stores
formatted glyphs as `(code point, Style)` pairs. Rendering takes the same
`visibleCharacters` value for every line in one ability, so lines reveal
simultaneously from left to right rather than one line after another.

Different abilities use `ability_stagger_ms` as a delay. The left and right
columns share the same elapsed clock and therefore start together; ability 2 is
delayed relative to ability 1 in its own column. Short blocks are kept visible
for at least 120 ms and long blocks are capped at 850 ms.

Content outside the initial viewport is not independently reanimated when it is
scrolled into view. It uses the same ability timeline and is already revealed if
its stagger interval has elapsed. This avoids starting hundreds of animations for
large fallback origins.

Enter, Space, or a click outside the confirmation control first skips the active
reveal. A second explicit confirmation action sends the normal Origins Legacy
packet.
