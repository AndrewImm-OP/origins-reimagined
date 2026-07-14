# Origins Overhaul

Клиент-серверный Fabric-мод поверх Origins Legacy для Minecraft 26.1.2.

Этап 4 добавляет frame-independent анимации экрана: fade-in, вращение иконки, reveal форматированных glyphs, stagger способностей, transitions с latest-target-wins, search overlay fade и reduce-motion. Модель игрока, visual layers, отношения с мобами и Phantom fix пока не реализованы.

Точная карта upstream API находится в [AUDIT_ORIGINS_LEGACY_26.1.2.md](AUDIT_ORIGINS_LEGACY_26.1.2.md), текущие ограничения — в [KNOWN_ISSUES.md](KNOWN_ISSUES.md).

## Требования и сборка

- Minecraft `26.1.2`;
- Java/JDK `25`;
- Fabric Loader/API;
- Origins Legacy `v1.12.10+26.1.2`, mod ID `origins-legacy` (также предоставляет alias `origins`).

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew --gradle-user-home .gradle-user-home build
```

Origins Legacy объявлен через Maven-координаты `xyz.bluspring:Origins-Legacy:1.12.15+26.1.2` из `https://mvn.devos.one/releases`.

Тестовый datapack находится в `test-datapack/`: он содержит неизвестный origin, дополнительный layer и сломанный presentation profile для проверки warning/fallback поведения.

Описание интеграции и геометрии экрана: [SELECTION_SCREEN_INTEGRATION.md](docs/SELECTION_SCREEN_INTEGRATION.md), [SELECTION_SCREEN_LAYOUT.md](docs/SELECTION_SCREEN_LAYOUT.md).

Описание анимационной системы: [ANIMATION_ARCHITECTURE.md](docs/ANIMATION_ARCHITECTURE.md), [TEXT_REVEAL_BEHAVIOR.md](docs/TEXT_REVEAL_BEHAVIOR.md).

Клиентские параметры читаются из `config/origins_overhaul.properties`; `cinematic_selection_screen=false` оставляет стандартный экран Origins Legacy.

Параметры анимаций включают `text_animation_enabled`, `text_animation_speed`, `transition_animation_enabled`, `transition_out_duration_ms`, `transition_in_duration_ms`, `icon_rotation_enabled`, `icon_rotation_speed`, `icon_bob_enabled`, `ability_stagger_ms` и `reduce_motion`.

Для debug-измерения catalog использовать JVM-флаг `-Dorigins_overhaul.debug=true`.
