# Selection screen integration

Origins Legacy `1.12.15+26.1.2` opens the selection UI in the private static method
`io.github.apace100.origins.networking.ModPacketsS2C.openOriginScreen(OpenOriginScreenPacket, ClientPlayNetworking.Context)`.
The method builds the enabled, not-yet-selected `OriginLayer` list and creates
`ChooseOriginScreen`. `ModPacketsS2C.receiveOriginConfirmation` later updates the
server-confirmed component and calls `WaitForNextLayerScreen.openSelection()`.

Origins: Reimagined injects at the head of `openOriginScreen`, cancels only the
client screen creation, and creates `CinematicOriginSelectionScreen` from the
client catalog and `OriginSelectionSession`. It does not change the payload
handlers or the server component.

Selections use the upstream `ChooseOriginPacket` and `ChooseRandomOriginPacket`.
After sending, the screen is replaced by `OriginsReimaginedWaitForNextLayerScreen`,
a small subclass of `WaitForNextLayerScreen`. Its overridden public
`openSelection()` advances the local session after upstream has accepted the
choice. Rejected choices therefore remain under Origins Legacy's normal
confirmation handling.

Mixin: `com.andrewimm.originsreimagined.mixin.ModPacketsS2CMixin`, client-only in
`origins_reimagined.mixins.json`. No common class imports client Minecraft APIs.

When `config/origins_reimagined.properties` contains
`cinematic_selection_screen=false`, the injection returns without cancelling and
Origins Legacy creates its original screen.
