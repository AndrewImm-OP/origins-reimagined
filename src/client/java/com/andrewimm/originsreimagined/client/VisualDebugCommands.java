package com.andrewimm.originsreimagined.client;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import com.andrewimm.originsreimagined.OriginsReimagined;
import com.andrewimm.originsreimagined.client.screen.VisualTestLabScreen;
import com.andrewimm.originsreimagined.client.screen.VisualWorldDebugScreen;

public final class VisualDebugCommands {
    private VisualDebugCommands() {}
    public static void register() {
        if (!OriginsReimagined.DEBUG) return;
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> register(dispatcher));
    }
    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("originsoverhaul")
            .then(ClientCommands.literal("debug")
                .then(ClientCommands.literal("visual_lab").executes(context -> { Minecraft.getInstance().setScreen(new VisualTestLabScreen()); return 1; }))
                .then(ClientCommands.literal("visual_world").executes(context -> { Minecraft.getInstance().setScreen(new VisualWorldDebugScreen()); return 1; }))));
    }
}
