package ru.origins_overhaul.client.visual.anchor;

public record EyeAnchorProfile(EyePreset preset, EyeAnchor leftEye, EyeAnchor rightEye) {
    public EyeAnchorProfile { preset = preset == null ? EyePreset.STANDARD : preset; }
    public static EyeAnchorProfile preset(EyePreset preset) { return new EyeAnchorProfile(preset, preset.left(), preset.right()); }
}
