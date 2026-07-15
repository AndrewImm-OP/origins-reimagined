# Elytra flight cancellation

Origins Overhaul handles `Ctrl+Shift` as an edge-triggered client input while
the local player is fall-flying and has the `origins:elytrian` origin.

On the first tick of the chord:

1. the client calls `stopFallFlying()`;
2. no movement vector, velocity, pose or position is changed;
3. the client sends `origins_overhaul:stop_elytra_flight` when the server
   advertises the payload;
4. the server validates the sender's current flight state and Elytrian origin,
   then stops fall-flying server-side.

The held-state guard makes this a single action per press. Releasing either key
arms the next press. Ordinary Shift and Ctrl without Shift do nothing.

The server handler is deliberately independent of the Origins selection
protocol. It is a small request, not a client-side origin assignment or a
replacement for Origins' fall-flying rules.
