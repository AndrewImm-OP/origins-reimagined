package com.andrewimm.originsreimagined.client.visual.render;

import net.minecraft.util.ARGB;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.player.PlayerModel;
import com.andrewimm.originsreimagined.client.visual.modifier.VisualModifier;
import com.andrewimm.originsreimagined.client.visual.profile.ResolvedVisualProfile;
import com.andrewimm.originsreimagined.client.preview.PlayerAppearanceSnapshot;

public final class VisualRenderBridge {
    private static final ThreadLocal<ResolvedVisualProfile> CURRENT = new ThreadLocal<>();
    private static volatile ResolvedVisualProfile PREVIEW = ResolvedVisualProfile.empty();
    private static volatile PlayerAppearanceSnapshot PREVIEW_APPEARANCE;
    private static volatile PlayerModel PREVIEW_MODEL;
    private VisualRenderBridge() {}
    public static void push(ResolvedVisualProfile profile) { CURRENT.set(profile); }
    public static void clear() { CURRENT.remove(); }
    public static void setPreview(ResolvedVisualProfile profile) { PREVIEW = profile == null ? ResolvedVisualProfile.empty() : profile; }
    public static void clearPreview() { PREVIEW = ResolvedVisualProfile.empty(); PREVIEW_APPEARANCE = null; PREVIEW_MODEL = null; }
    public static void setPreviewAppearance(PlayerAppearanceSnapshot appearance) { PREVIEW_APPEARANCE = appearance; }
    public static PlayerAppearanceSnapshot previewAppearance() { return PREVIEW_APPEARANCE; }
    public static void setPreviewModel(PlayerModel model) { PREVIEW_MODEL = model; }
    public static boolean isPreviewModel(Model<?> model) { return PREVIEW_MODEL == model; }
    public static ResolvedVisualProfile profile() {
        ResolvedVisualProfile profile = CURRENT.get();
        return profile == null ? PREVIEW : profile;
    }
    public static int tint() {
        ResolvedVisualProfile profile = profile();
        if (profile == null) return 0xFFFFFFFF;
        int tint = 0xFFFFFFFF;
        for (VisualModifier modifier : profile.modifiers()) if (modifier.type().getPath().equals("model_tint")) tint = ARGB.multiply(tint, ARGB.srgbLerp(modifier.strength(), 0xFFFFFFFF, modifier.color()));
        return tint;
    }
}
