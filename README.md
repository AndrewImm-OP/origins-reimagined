# Origins: Reimagined

Клиент-серверный Fabric-мод для Minecraft `26.1.2`, расширяющий [Origins: Legacy](https://modrinth.com/mod/origins-legacy) новым экраном выбора, presentation-профилями, preview игрока, визуальным pipeline и дополнительными косметическими механиками.

Внутренний mod ID проекта остаётся `origins_overhaul` для совместимости с ресурсами, конфигурацией, datapack-путями и уже существующими мирами.

## Что добавляет мод

### Новый экран выбора происхождения

- заменяет стандартное представление Origins Legacy, не меняя серверный selection flow;
- показывает название и иконку выбранного origin;
- окрашивает заголовок по impact/difficulty;
- разделяет преимущества и недостатки по левому и правому столбцу;
- показывает нейтральные особенности отдельно;
- поддерживает перенос длинных описаний и независимую прокрутку колонок;
- поддерживает несколько origin layers;
- сохраняет стандартную отправку выбора и случайного выбора Origins Legacy;
- работает с неизвестными origins и origins из аддонов через fallback;
- при отключении cinematic screen возвращает стандартный экран Origins Legacy.

### Анимации интерфейса

- frame-independent animation clock на monotonic time;
- плавное появление фона, заголовка, колонок и кнопок;
- одновременное раскрытие строк одного описания;
- последовательное появление способностей сверху вниз;
- плавные переходы между origins;
- защита от спама переключения через latest-target-wins;
- режим `reduce_motion` и полное отключение анимаций;
- вращение и лёгкое покачивание иконки происхождения.

### Интерактивное preview игрока

- отображает настоящий скин текущего игрока;
- поддерживает classic и slim model;
- вращение ЛКМ;
- приближение и отдаление колёсиком;
- перемещение ПКМ;
- сброс камеры двойным кликом или клавишей `R`;
- автоматическое вращение до первого взаимодействия;
- управление outer skin layer;
- clipping внутри центральной preview-области;
- preview не изменяет настоящую entity игрока и не добавляет entity в мир.

### Presentation pipeline

Origins: Reimagined строит immutable snapshot-модель origin поверх синхронизированных данных Origins Legacy.

- presentation profiles загружаются из JSON и поддерживают resource-pack override;
- можно объединять несколько powers в одну особенность;
- технические powers можно скрывать;
- отсутствующие профили используют безопасный fallback;
- неизвестные powers, переводы, иконки и повреждённые JSON не приводят к crash;
- встроены профили для стандартных origins, включая Human, Phantom, Enderian, Arachnid, Blazeborn, Merling, Feline, Avian, Elytrian и Shulk;
- доступен debug catalog и Visual Test Lab.

### Visual profiles

Visual pipeline использует общий resolved profile для preview и world rendering.

Поддерживается архитектура для:

- tint-слоёв;
- texture overlays;
- eye anchors и presets;
- emissive overlays;
- model attachments;
- segmented geometry;
- particle aura;
- условий `power_active`, `preview`, `underwater`, `sneaking` и других состояний;
- безопасного отключения неподдерживаемых capabilities.

Часть сложных visual passes всё ещё требует дополнительной графической проверки в настоящем клиенте. При отсутствии или повреждении профиля игрок отображается обычным скином.

### Дополнительная механика Elytrian

Во время активного полёта на элитрах сочетание `Ctrl + Shift` отменяет fall-flying:

- срабатывает один раз на новое нажатие;
- обычный `Shift` не отменяет полёт;
- текущая скорость и направление сохраняются;
- игрок переходит в обычное свободное падение;
- сервер валидирует, что игрок действительно летит и имеет `origins:elytrian`;
- не изменяются powers, origin assignment или серверные правила Origins Legacy.

## Архитектура

- `com.andrewimm.originsreimagined` — Java package проекта;
- common source set содержит модель, adapter, reload pipeline и серверную механику;
- client source set содержит экран, анимации, preview, visual renderers и client mixins;
- прямой доступ к Origins Legacy изолирован в `compat/originslegacy`;
- собственный registry origins не создаётся;
- server-side проверка Origins Legacy остаётся источником истины.

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

Visual Test Lab: [VISUAL_TEST_LAB.md](docs/VISUAL_TEST_LAB.md), [GRAPHICAL_VALIDATION_CHECKLIST.md](docs/GRAPHICAL_VALIDATION_CHECKLIST.md), [SEGMENTED_GEOMETRY.md](docs/SEGMENTED_GEOMETRY.md), [PREVIEW_PARTICLES.md](docs/PREVIEW_PARTICLES.md).

Клиентские параметры читаются из `config/origins_overhaul.properties`; `cinematic_selection_screen=false` оставляет стандартный экран Origins Legacy.

Параметры анимаций включают `text_animation_enabled`, `text_animation_speed`, `transition_animation_enabled`, `transition_out_duration_ms`, `transition_in_duration_ms`, `icon_rotation_enabled`, `icon_rotation_speed`, `icon_bob_enabled`, `ability_stagger_ms` и `reduce_motion`. Preview управляется `player_preview_enabled`, `preview_auto_rotate`, `preview_auto_rotate_speed`, `preview_mouse_sensitivity`, `preview_zoom_sensitivity`, `preview_show_outer_layer`, `preview_show_cape`, `preview_show_equipment` и `preview_idle_animation`.

Для debug-измерения catalog использовать JVM-флаг `-Dorigins_overhaul.debug=true`.

## Debug-команды

При запуске с `-Dorigins_overhaul.debug=true` доступны:

```text
/originsoverhaul debug catalog
/originsoverhaul debug open_selection
/originsoverhaul debug visual_lab
/originsoverhaul debug visual_world
```

Debug-команды не назначают origin напрямую и не обходят серверную проверку.

## Текущий статус

Рабочий проект собирается на JDK 25. Изолированные тесты, client compilation, packaged mixin verification и dedicated-server common classloading подготовлены. Полноценная совместимость всех visual passes с Sodium/Iris и графическая проверка каждого профиля требуют отдельного smoke-test в настоящем клиенте.

Планируемые направления:

- стабилизация визуальных render passes;
- дополнительные тематические UI-профили происхождений;
- улучшение geometry attachments и particles;
- mob relations;
- отдельные исправления механик Origins после завершения визуального pipeline.
