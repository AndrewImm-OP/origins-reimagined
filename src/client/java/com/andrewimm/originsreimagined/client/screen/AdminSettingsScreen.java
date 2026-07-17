package com.andrewimm.originsreimagined.client.screen;

import com.andrewimm.originsreimagined.gameplay.MerlingConfig;
import com.andrewimm.originsreimagined.networking.UpdateAdminSettingPayload;
import com.andrewimm.originsreimagined.networking.UpdateAdminFeaturePayload;
import com.andrewimm.originsreimagined.client.ClientOriginCatalog;
import com.andrewimm.originsreimagined.model.OriginPresentation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A deliberately vanilla-looking admin editor. Values are still validated server-side. */
public final class AdminSettingsScreen extends Screen {
    private final List<String> settings = new ArrayList<>(MerlingConfig.knownSettings().keySet().stream().sorted().toList());
    private final List<String> origins = List.of("origins:arachnid", "origins:feline", "origins:phantom", "origins:merling", "origins:elytrian", "origins:avian", "origins:blazeborn", "origins:enderian", "origins:shulk", "origins:human");
    private final Map<String, String> localValues = new java.util.HashMap<>(MerlingConfig.knownSettings());
    private int page;
    private String selectedOrigin = "origins:arachnid";
    private boolean featureMode;
    private final Map<String, Boolean> localFeatureState = new java.util.HashMap<>();

    public AdminSettingsScreen() { super(Component.literal("Origins: Reimagined Admin Settings")); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xFF202020);
        int left = width / 2 - 230, top = Math.max(12, height / 2 - 125);
        int panelWidth = 460, panelHeight = 250;
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xFF8B8B8B);
        graphics.fill(left + 4, top + 4, left + panelWidth - 4, top + panelHeight - 4, 0xFF373737);
        graphics.text(font, Component.literal("ORIGINS: REIMAGINED — ADMIN"), left + 12, top + 10, 0xFFFFFFFF, false);
        graphics.text(font, Component.literal("Происхождение: " + selectedOrigin), left + 122, top + 28, 0xFFB8FFB8, false);
        graphics.text(font, Component.literal("F: " + (featureMode ? "особенности" : "числовые параметры") + " · значения применяются сразу"), left + 12, top + 42, 0xFFAAAAAA, false);

        int rowTop = top + 62;
        for (int i = 0; i < origins.size(); i++) {
            int y = top + 59 + i * 17;
            boolean selected = origins.get(i).equals(selectedOrigin);
            graphics.fill(left + 8, y - 2, left + 105, y + 13, selected ? 0xFF4B4B4B : 0xFF292929);
            graphics.text(font, Component.literal(origins.get(i).replace("origins:", "")), left + 13, y + 1, selected ? 0xFFFFFFFF : 0xFFAAAAAA, false);
        }
        List<String> visible = featureMode ? featureIds() : settings.stream().filter(this::belongsToSelectedOrigin).toList();
        for (int i = 0; i < 6 && page * 6 + i < visible.size(); i++) {
            String key = visible.get(page * 6 + i);
            String value = featureMode ? (localFeatureState.getOrDefault(selectedOrigin + "|" + key, true) ? "ВКЛ" : "ВЫКЛ") : localValues.getOrDefault(key, "?");
            int y = rowTop + i * 27;
            graphics.fill(left + 10, y - 3, left + panelWidth - 10, y + 20, 0xFF262626);
            graphics.text(font, Component.literal(key), left + 16, y + 2, 0xFFE0E0E0, false);
            graphics.text(font, Component.literal(featureMode ? "" : "−"), left + 315, y + 2, 0xFFFFFFFF, false);
            graphics.text(font, Component.literal(value), left + 340, y + 2, 0xFFFFD37A, false);
            graphics.text(font, Component.literal(featureMode ? "" : "+"), left + 390, y + 2, 0xFFFFFFFF, false);
        }
        if (visible.isEmpty()) graphics.text(font, Component.literal("Числовых параметров нет"), left + 122, top + 82, 0xFFAAAAAA, false);
        graphics.text(font, Component.literal("‹  страница " + (page + 1) + "/" + Math.max(1, (visible.size() + 5) / 6) + "  ›"), left + 122, top + 229, 0xFFCCCCCC, false);
        graphics.text(font, Component.literal("ЛКМ: изменить   ←/→: страница   Esc: закрыть"), left + 205, top + 229, 0xFFAAAAAA, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true;
        int left = width / 2 - 230, top = Math.max(12, height / 2 - 125);
        if (event.x() >= left + 8 && event.x() <= left + 105 && event.y() >= top + 57 && event.y() < top + 57 + origins.size() * 17) {
            selectedOrigin = origins.get((int) ((event.y() - (top + 57)) / 17));
            page = 0;
            return true;
        }
        List<String> visible = settings.stream().filter(this::belongsToSelectedOrigin).toList();
        int row = (int) ((event.y() - (top + 59)) / 27);
        if (event.x() >= left + 300 && event.x() <= left + 430 && row >= 0 && row < 6) {
            int index = page * 6 + row;
            if (index < visible.size()) {
                if (index >= visible.size()) return true;
                String key = visible.get(index);
                if (featureMode) {
                    String stateKey = selectedOrigin + "|" + key;
                    boolean enabled = !localFeatureState.getOrDefault(stateKey, true);
                    localFeatureState.put(stateKey, enabled);
                    if (ClientPlayNetworking.canSend(UpdateAdminFeaturePayload.TYPE)) {
                        ClientPlayNetworking.send(new UpdateAdminFeaturePayload(net.minecraft.resources.Identifier.parse(selectedOrigin), net.minecraft.resources.Identifier.parse(key), enabled));
                    }
                    return true;
                }
                double current = Double.parseDouble(localValues.get(key));
                double step = key.endsWith("_ticks") ? 20.0D : 0.05D;
                double next = event.x() < left + 330 ? current - step : current + step;
                if (next < 0.0D) next = 0.0D;
                localValues.put(key, key.endsWith("_ticks") ? Long.toString(Math.round(next)) : String.format(java.util.Locale.ROOT, "%.2f", next));
                if (ClientPlayNetworking.canSend(UpdateAdminSettingPayload.TYPE)) {
                    ClientPlayNetworking.send(new UpdateAdminSettingPayload(key, next));
                }
            }
            return true;
        }
        return true;
    }

    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        if (event.key() == GLFW.GLFW_KEY_F) { featureMode = !featureMode; page = 0; return true; }
        int visibleCount = (int) settings.stream().filter(this::belongsToSelectedOrigin).count();
        if (event.key() == GLFW.GLFW_KEY_RIGHT) { page = Math.min(page + 1, Math.max(0, (visibleCount - 1) / 6)); return true; }
        if (event.key() == GLFW.GLFW_KEY_LEFT) { page = Math.max(0, page - 1); return true; }
        return true;
    }

    @Override public boolean shouldCloseOnEsc() { return true; }

    private boolean belongsToSelectedOrigin(String key) {
        return key.startsWith(selectedOrigin.substring(selectedOrigin.indexOf(':') + 1) + ".")
            || (selectedOrigin.equals("origins:merling") && key.startsWith("merling."));
    }

    private List<String> featureIds() {
        List<String> result = new ArrayList<>();
        for (var layer : ClientOriginCatalog.getLayers()) {
            for (OriginPresentation origin : ClientOriginCatalog.getOrigins(layer.id())) {
                if (!origin.originId().toString().equals(selectedOrigin)) continue;
                origin.advantages().forEach(power -> result.addAll(power.representedPowerIds().stream().map(Object::toString).toList()));
                origin.disadvantages().forEach(power -> result.addAll(power.representedPowerIds().stream().map(Object::toString).toList()));
                origin.neutralFeatures().forEach(power -> result.addAll(power.representedPowerIds().stream().map(Object::toString).toList()));
            }
        }
        return result.stream().distinct().sorted().toList();
    }
}
