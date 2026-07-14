# Built-in visual profiles

Resource profiles exist for Human, Phantom, Enderian, Arachnid, Blazeborn, Merling, Feline, Avian, Elytrian and Shulk. Human is intentionally empty. Phantom includes a tint, a conditional Phantom Form alpha declaration and preview states; the remaining profiles provide safe tint/preview defaults until their dedicated geometry and texture assets are implemented.

This distinction is deliberate: an origin with a profile must not receive a fake attachment merely because its name is thematic. Missing or incomplete profiles always fall back to the normal player skin.
