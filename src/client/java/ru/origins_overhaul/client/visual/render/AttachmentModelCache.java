package ru.origins_overhaul.client.visual.render;

import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import ru.origins_overhaul.client.visual.modifier.VisualModifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AttachmentModelCache {
    private static final Map<String, ModelPart> CACHE = new ConcurrentHashMap<>();
    private AttachmentModelCache() {}

    public static ModelPart get(VisualModifier modifier, boolean slim) {
        String key = modifier.id() + ":" + (slim ? "slim" : "classic") + ":" + java.util.Arrays.toString(modifier.geometrySize()) + ":" + java.util.Arrays.toString(modifier.uv());
        return CACHE.computeIfAbsent(key, ignored -> bake(modifier));
    }

    public static void clear() { CACHE.clear(); }

    private static ModelPart bake(VisualModifier modifier) {
        float[] size = modifier.geometrySize();
        float width = Math.max(0.01f, size[0] * 8.0f);
        float height = Math.max(0.01f, size[1] * 8.0f);
        float depth = Math.max(0.01f, size[2] * 8.0f);
        String type = modifier.geometryType();
        if (type.equals("plane") || type.equals("cross_planes")) depth = 0.05f;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeListBuilder cube = CubeListBuilder.create().texOffs(modifier.uv()[0], modifier.uv()[1])
            .addBox(-width / 2.0f, -height / 2.0f, -depth / 2.0f, width, height, depth);
        root.addOrReplaceChild("attachment", cube, PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }
}
