package com.andrewimm.originsreimagined.client;

import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.networking.StickyThreadsPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** Client input for the server-validated Arachnid ability. */
public final class StickyThreadsInput {
    private static KeyMapping key;

    private StickyThreadsInput() {}

    public static void register() {
        key = new KeyMapping(
            "key.origins_reimagined.sticky_threads", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G, KeyMapping.Category.register(OriginsReimagined.id("key_category")));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (key.consumeClick()) {
                if (client.player != null && ClientPlayNetworking.canSend(StickyThreadsPayload.TYPE)) {
                    ClientPlayNetworking.send(new StickyThreadsPayload());
                }
            }
        });
        OriginsReimagined.LOGGER.debug("Sticky Threads key binding registered");
    }
}
