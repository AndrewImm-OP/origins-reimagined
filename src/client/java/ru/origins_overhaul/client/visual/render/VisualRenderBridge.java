package ru.origins_overhaul.client.visual.render;

import net.minecraft.util.ARGB;
import ru.origins_overhaul.client.visual.modifier.VisualModifier;
import ru.origins_overhaul.client.visual.profile.ResolvedVisualProfile;

public final class VisualRenderBridge {
    private static final ThreadLocal<ResolvedVisualProfile> CURRENT = new ThreadLocal<>();
    private VisualRenderBridge() {}
    public static void push(ResolvedVisualProfile profile) { CURRENT.set(profile); }
    public static void clear() { CURRENT.remove(); }
    public static int tint() {
        ResolvedVisualProfile profile = CURRENT.get();
        if (profile == null) return 0xFFFFFFFF;
        int tint = 0xFFFFFFFF;
        for (VisualModifier modifier : profile.modifiers()) if (modifier.type().getPath().equals("model_tint")) tint = ARGB.multiply(tint, modifier.color());
        return tint;
    }
}
