# Elytra flight cancellation

Origins: Reimagined handles `Space` as an edge-triggered client input while
the local player is fall-flying and has the `origins:elytrian` origin.

On the first tick of the Space press:

1. the client calls `stopFallFlying()`;
2. no movement vector, velocity, pose or position is changed;
3. the client sends `origins_reimagined:stop_elytra_flight` when the server
   advertises the payload;
4. the server validates the sender's current flight state and Elytrian origin,
   then stops fall-flying server-side.

The held-state guard makes this a single action per press. Releasing Space arms
the next press. Space has no effect unless the player is currently fall-flying.

The server handler is deliberately independent of the Origins selection
protocol. It is a small request, not a client-side origin assignment or a
replacement for Origins' fall-flying rules.
