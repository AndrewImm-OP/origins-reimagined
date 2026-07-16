# Origins: Reimagined

> Улучшенный клиент-серверный Fabric-мод для Origins: Legacy — новый экран выбора, универсальные presentation-профили, интерактивный preview игрока и дополнительные визуальные механики.

[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-3f8f3f?style=flat-square)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Mod%20loader-Fabric-dbd0b3?style=flat-square)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-e76f00?style=flat-square)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

Origins: Reimagined развивается поверх Origins: Legacy и сохраняет его серверные правила, синхронизацию origins и стандартные payload’ы выбора. Мод меняет клиентское представление и добавляет отдельные безопасные механики, не создавая собственную систему назначения origins.

> Новый внутренний `mod_id` проекта — `origins_reimagined`. Все проектные resource IDs, конфигурации, datapack-пути, debug-команды и архивы теперь используют имя Origins: Reimagined.

## Навигация

- [Возможности](#возможности)
- [Что именно исправляет мод](#что-именно-исправляет-мод)
- [Origins и presentation-профили](#origins-и-presentation-профили)
- [Механика Elytrian](#механика-elytrian)
- [Переработка Merling](#переработка-merling)
- [Защита Phantom от солнца](#защита-phantom-от-солнца)
- [Установка](#установка)
- [Сборка из исходников](#сборка-из-исходников)
- [Настройки](#настройки)
- [Debug-команды](#debug-команды)
- [Архитектура](#архитектура)
- [Статус и планы](#статус-и-планы)
- [Ограничения](#ограничения)

## Возможности

### Экран выбора origin

Новый экран открывается через обычный процесс Origins Legacy и использует уже синхронизированные сервером данные.

- заголовок origin с иконкой и цветом сложности;
- преимущества слева, недостатки справа;
- нейтральные особенности в отдельной прокручиваемой области;
- перенос длинных названий и описаний без выхода за границы колонок;
- поддержка нескольких origin layers;
- переключение клавишами, мышью и колёсиком;
- быстрый список и поиск для больших наборов origins;
- стандартный выбор и случайный выбор через Origins Legacy;
- fallback для неизвестных origins и аддонов;
- отсутствие dirt background;
- стандартный экран возвращается при `cinematic_selection_screen=false`.

Экран не назначает origin на клиенте и не заменяет серверную проверку Origins Legacy.

### Анимации

Интерфейс использует отдельную time-based animation system, поэтому скорость не зависит от FPS.

- плавное затемнение игрового мира;
- появление header, колонок и кнопок;
- раскрытие всех строк одного описания одновременно;
- stagger-анимация способностей сверху вниз;
- направленные переходы между origins;
- latest-target-wins при быстром переключении;
- плавное вращение и покачивание иконки;
- независимая прокрутка текстовых колонок;
- `reduce_motion` и режим полного отключения анимаций.

### Интерактивный preview игрока

В центральной области отображается настоящий скин текущего игрока.

| Управление | Действие |
| --- | --- |
| ЛКМ + движение | вращение модели |
| ПКМ + движение | перемещение модели |
| Колесо над preview | zoom |
| Двойной клик | сброс камеры |
| `R` | сброс камеры |

Поддерживаются classic и slim skin model, outer skin layer и автоматическое вращение до первого взаимодействия. Preview использует отдельное состояние и не изменяет настоящую player entity, её позицию, rotation, pose, equipment или gameplay state.

### Visual pipeline

Один resolved visual profile используется и в preview, и в world rendering. Система рассчитана на:

- tint и texture overlays;
- eye anchors и presets;
- emissive overlays;
- attachments на model-part anchors;
- cuboid, plane, cross-plane и segmented geometry;
- простые idle/breath/sway-анимации attachments;
- particle aura с ограничением частоты и дистанции;
- условия `power_active`, `preview`, `underwater`, `sneaking`, `swimming` и другие состояния;
- capability detection и безопасный пропуск неподдерживаемых passes.

Встроенные origins: Human, Phantom, Enderian, Arachnid, Blazeborn, Merling, Feline, Avian, Elytrian и Shulk.

Система не изменяет исходный PNG-скин. При отсутствии профиля, текстуры или корректного modifier игрок остаётся отображаться обычным скином.

### Переработка Merling

Merling получает водную специализацию без запрета на обычную наземную игру:

- не теряет воздух на суше и не получает урон от отсутствия воздуха;
- сохраняет штатное быстрое плавание Origins Legacy;
- наносит на 20% больше урона, когда сам находится в воде;
- не может употреблять рыбу из расширяемого item tag;
- получает в 1,5 раза больше фактически прошедшего огненного урона;
- после десятисекундной задержки постепенно получает отдельный урон от высыхания в Незере.

Таймер Незера, множители урона и запрещённая еда обрабатываются сервером.
Подробности: [docs/MERLING_REWORK.md](docs/MERLING_REWORK.md).

### Защита Phantom от солнца

Солнечная механика Phantom переработана так, чтобы шлем действительно был временной защитой, а не отключал ограничение навсегда:

- надетый шлем предотвращает новое солнечное горение Phantom при прямом свете;
- шлем теряет фиксированные `5` единиц прочности раз в `1200` тиков — один раз в игровую минуту;
- `Unbreaking` и другие зачарования не уменьшают этот специальный износ;
- если прочности осталось меньше пяти, шлем ломается;
- после разрушения шлема солнечное горение снова начинает поддерживаться;
- защита и износ работают только при открытом небе, дневном свете и без дождя;
- механика выполняется на сервере и не меняет поведение Phantom в тени, ночью или под блоками.

Это отдельная серверная логика поверх стандартной power Origins Legacy. Остальные origins, обычный огонь и обычная прочность предметов не затрагиваются.

## Что именно исправляет мод

### Исправлено в интерфейсе

- описания больше не должны рисоваться поверх соседних колонок;
- длинный текст переносится по ширине доступной области;
- преимущества и недостатки разделены по смыслу и цвету;
- neutral features не смешиваются с преимуществами и недостатками;
- большие списки не требуют отображать сотни origins одновременно;
- preview имеет clipping и не должен перекрывать controls;
- selection controls не вмешиваются в сетевой flow Origins Legacy;
- при отключении нового экрана используется upstream UI.

### Исправлено в структуре проекта

- common и client source sets разделены;
- client mixins не загружаются на dedicated server;
- Origins Legacy API изолирован в compatibility adapter;
- серверные origins не копируются в собственный registry;
- snapshots используют immutable/defensive data;
- resource reload очищает и перестраивает presentation/visual caches;
- повреждённые профили и неизвестные powers пропускаются с warning вместо crash.

### Добавлена отдельная механика Elytrian

Во время активного полёта на элитрах `Ctrl + Shift` отменяет режим планирования.

- срабатывает один раз на новое нажатие сочетания;
- обычный `Shift` не отменяет полёт;
- текущие скорость и направление сохраняются;
- игрок переходит в обычное свободное падение;
- можно использовать механику падения, включая булаву;
- не добавляется импульс вверх или вниз;
- сервер принимает только валидированный запрос от реально летящего Elytrian;
- назначение origin и правила Origins Legacy не изменяются.

## Origins и presentation-профили

Presentation profile описывает, как origin показывается игроку, но не меняет его gameplay.

Профиль может:

- объединять несколько powers в одну понятную способность;
- раскладывать powers на преимущества, недостатки и нейтральные особенности;
- скрывать технические powers;
- задавать собственное имя и описание;
- переопределять цвет сложности;
- связывать origin с visual profile;
- переживать resource-pack override по обычному порядку ресурсов.

Если профиль отсутствует, применяется автоматический fallback:

- имя берётся из origin или ID;
- impact берётся из Origins Legacy;
- все доступные powers показываются нейтральными особенностями;
- технические данные не приводят к ошибке;
- отсутствующий перевод заменяется безопасным текстом на основе ID.

Для проверки pipeline в репозитории есть тестовый datapack с unknown origin, дополнительным layer и broken profile.

## Установка

### Требования

- Minecraft `26.1.2`;
- Java/JDK `25`;
- Fabric Loader;
- Fabric API;
- Origins: Legacy `1.12.15+26.1.2`.

Origins: Reimagined не включает Origins Legacy внутрь JAR. Зависимость подключается как обязательная Fabric mod dependency из Maven-репозитория разработчика:

```text
xyz.bluspring:Origins-Legacy:1.12.15+26.1.2
```

### Установка готового JAR

1. Установить Fabric Loader для Minecraft `26.1.2`.
2. Установить Fabric API.
3. Установить Origins: Legacy.
4. Положить `origins-reimagined-*.jar` в папку `mods`.
5. Запустить клиент или dedicated server.

## Сборка из исходников

Проект использует JDK 25 и локальный Gradle cache:

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
export PATH="$JAVA_HOME/bin:$PATH"

./gradlew --gradle-user-home .gradle-user-home build
```

Готовый артефакт появится в:

```text
build/libs/origins-reimagined-0.1.0+26.1.2.jar
```

Проверяются client/common compilation, self-tests и наличие packaged mixin-классов.

## Настройки

Конфигурация клиента хранится в:

```text
config/origins_reimagined.properties
```

Основные параметры:

```properties
cinematic_selection_screen=true
selection_background_opacity=0.70
show_neutral_features=true
origin_list_threshold=12

text_animation_enabled=true
text_animation_speed=42.0
transition_animation_enabled=true
reduce_motion=false

player_preview_enabled=true
preview_auto_rotate=true
preview_auto_rotate_speed=12.0
preview_show_outer_layer=true
preview_show_cape=false
preview_show_equipment=false

origin_visuals_enabled=true
visual_overlays_enabled=true
visual_attachments_enabled=true
visual_emissive_enabled=true
visual_particles_enabled=true
show_other_player_visuals=true
show_other_player_particles=false

# Server-side Merling settings
merling.underwater_damage_multiplier=1.20
merling.fire_damage_multiplier=1.50
merling.nether_grace_ticks=200
merling.nether_damage_interval_ticks=40
merling.nether_damage=1.0
```

Серверные параметры создаются в `config/origins_reimagined.properties` на стороне сервера. Значения для Phantom износа шлема пока являются фиксированным игровым правилом: `5` прочности раз в `1200` тиков.

Некорректные значения ограничиваются безопасным диапазоном или заменяются значением по умолчанию.

## Debug-команды

Команды доступны при запуске с JVM-флагом:

```text
-Dorigins_reimagined.debug=true
```

```text
/originsreimagined debug catalog
/originsreimagined debug open_selection
/originsreimagined debug visual_lab
/originsreimagined debug visual_world
```

Debug preview не отправляет настоящий выбор origin. Команды не обходят серверную проверку.

## Архитектура

```text
origins-reimagined/
├── src/main/java/com/andrewimm/originsreimagined/
│   ├── compat/originslegacy/   # единственная точка прямого доступа к Origins Legacy
│   ├── gameplay/               # server-validated gameplay requests
│   ├── model/                  # immutable snapshots
│   ├── networking/             # custom payloads
│   └── profiles/               # presentation reload pipeline
├── src/client/java/com/andrewimm/originsreimagined/
│   ├── animation/              # time-based UI animation
│   ├── preview/                # player preview and camera
│   ├── screen/                 # selection screen and Visual Test Lab
│   ├── visual/                 # profiles, modifiers and render backends
│   └── mixin/                  # client-only integration points
├── src/main/resources/
│   ├── assets/                  # client visual profiles and localization
│   └── data/                    # presentation profiles
└── test-datapack/               # fallback/reload test resources
```

## Статус и планы

Проект находится в активной разработке. Уже реализованы фундамент catalog/presentation pipeline, новый selection screen, анимации, интерактивный player preview, visual profile architecture и механика отмены Elytrian flight.

### Дальше в работе

- стабилизация настоящих texture/emissive/alpha passes;
- графическая проверка Sodium/Iris и разных skin types;
- улучшение geometry attachments и preview particles;
- дополнительные тематические оформления origins;
- mob relations;
- отдельные исправления механик Origins после завершения visual pipeline.

### Важно

Графическая проверка всех visual profiles в настоящем клиенте Minecraft ещё не считается завершённой. В headless-среде доступны сборка, client compilation, isolated tests и dedicated-server проверки, но они не заменяют ручной визуальный smoke-test.

## Лицензия и автор

MIT License.

Автор: **AndrewImm**.
