package ru.origins_overhaul.client.visual.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import ru.origins_overhaul.client.preview.PlayerAppearanceSnapshot;
import ru.origins_overhaul.client.visual.context.WorldPlayerVisualContext;
import ru.origins_overhaul.client.visual.modifier.VisualModifier;
import ru.origins_overhaul.client.visual.profile.ResolvedVisualProfile;
import ru.origins_overhaul.client.visual.profile.VisualProfileResolver;

public final class WorldVisualRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public WorldVisualRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
        Minecraft client = Minecraft.getInstance();
        if (!ru.origins_overhaul.client.ClientSelectionConfig.visualsEnabled() || client.level == null) return;
        if (!(client.level.getEntity(state.id) instanceof Player player)) return;
        if (!ru.origins_overhaul.client.ClientSelectionConfig.showOtherVisuals() && player != client.player) return;
        if (!(player instanceof net.minecraft.client.player.AbstractClientPlayer clientPlayer)) return;
        PlayerAppearanceSnapshot appearance = PlayerAppearanceSnapshot.from(clientPlayer, client.options);
        WorldPlayerVisualContext context = new WorldPlayerVisualContext(player, appearance, limbDistance);
        ResolvedVisualProfile profile = VisualProfileResolver.resolve(context, true);
        for (VisualModifier modifier : profile.modifiers()) {
            String type = modifier.type().getPath();
            if (!type.equals("texture_overlay") && !type.equals("emissive_overlay")) continue;
            Identifier texture = modifier.texture();
            if (texture == null) continue;
            int color = ARGB.multiplyAlpha(modifier.color(), modifier.opacity());
            RenderLayer.renderColoredCutoutModel(getParentModel(), texture, poseStack, collector, light, state, color, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        }
    }
}
