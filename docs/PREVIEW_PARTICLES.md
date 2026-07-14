# Preview particles

`PreviewParticleSystem` is a local screen-space diagnostic emitter. It owns its particles, caps the count at 256, accumulates emission rate, respects density/reduced-motion settings and is cleared when the lab or preview closes. It never calls `ClientLevel.addParticle`, so it cannot leak particles into the world. World aura emission remains handled separately by `ParticleAuraManager`.
