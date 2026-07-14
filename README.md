# Origins Overhaul

Клиент-серверный Fabric-мод поверх Origins Legacy для Minecraft 26.1.2.

Этап 6 добавляет общий visual-profile pipeline поверх интерактивного preview: профили ресурсов, условия, tint и безопасные world/preview backend’ы для wide/slim skin. Дополнительная геометрия, полноценный emissive/alpha backend, частицы и отношения с мобами пока не реализованы.

Точная карта upstream API находится в [AUDIT_ORIGINS_LEGACY_26.1.2.md](AUDIT_ORIGINS_LEGACY_26.1.2.md), текущие ограничения — в [KNOWN_ISSUES.md](KNOWN_ISSUES.md).

## Требования и сборка

- Minecraft `26.1.2`;
- Java/JDK `25`;
- Fabric Loader/API;
- Origins Legacy `v1.12.15+26.1.2`, mod ID `origins-legacy` (также предоставляет alias `origins`).

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew --gradle-user-home .gradle-user-home build
```

Origins Legacy объявлен через Maven-координаты `xyz.bluspring:Origins-Legacy:1.12.15+26.1.2` из `https://mvn.devos.one/releases`.

Тестовый datapack находится в `test-datapack/`: он содержит неизвестный origin, дополнительный layer и сломанный presentation profile для проверки warning/fallback поведения.

Описание интеграции и геометрии экрана: [SELECTION_SCREEN_INTEGRATION.md](docs/SELECTION_SCREEN_INTEGRATION.md), [SELECTION_SCREEN_LAYOUT.md](docs/SELECTION_SCREEN_LAYOUT.md).

Описание анимационной системы: [ANIMATION_ARCHITECTURE.md](docs/ANIMATION_ARCHITECTURE.md), [TEXT_REVEAL_BEHAVIOR.md](docs/TEXT_REVEAL_BEHAVIOR.md).

Аудит и устройство preview: [PLAYER_PREVIEW_RENDERING_AUDIT.md](docs/PLAYER_PREVIEW_RENDERING_AUDIT.md), [PLAYER_PREVIEW_ARCHITECTURE.md](docs/PLAYER_PREVIEW_ARCHITECTURE.md), [PLAYER_PREVIEW_CONTROLS.md](docs/PLAYER_PREVIEW_CONTROLS.md).

Visual pipeline и формат профилей: [VISUAL_RENDERING_PIPELINE_AUDIT.md](docs/VISUAL_RENDERING_PIPELINE_AUDIT.md), [VISUAL_PROFILE_FORMAT.md](docs/VISUAL_PROFILE_FORMAT.md), [VISUAL_MODIFIER_TYPES.md](docs/VISUAL_MODIFIER_TYPES.md), [SKIN_ANCHOR_SYSTEM.md](docs/SKIN_ANCHOR_SYSTEM.md), [BUILTIN_VISUAL_PROFILES.md](docs/BUILTIN_VISUAL_PROFILES.md).

Клиентские параметры читаются из `config/origins_overhaul.properties`; `cinematic_selection_screen=false` оставляет стандартный экран Origins Legacy.

Параметры анимаций включают `text_animation_enabled`, `text_animation_speed`, `transition_animation_enabled`, `transition_out_duration_ms`, `transition_in_duration_ms`, `icon_rotation_enabled`, `icon_rotation_speed`, `icon_bob_enabled`, `ability_stagger_ms` и `reduce_motion`. Preview управляется `player_preview_enabled`, `preview_auto_rotate`, `preview_auto_rotate_speed`, `preview_mouse_sensitivity`, `preview_zoom_sensitivity`, `preview_show_outer_layer`, `preview_show_cape`, `preview_show_equipment` и `preview_idle_animation`.

Для debug-измерения catalog использовать JVM-флаг `-Dorigins_overhaul.debug=true`.
