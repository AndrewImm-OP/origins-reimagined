# Origins: Reimagined

> Клиент-серверный Fabric-аддон для Origins: Legacy, который заменяет экран выбора происхождения и точечно изменяет игровые механики стандартных Origins.

![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-62B47A?style=flat-square)
![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4?style=flat-square)
![Java](https://img.shields.io/badge/Java-25-ED8B00?style=flat-square)
![Version](https://img.shields.io/badge/Version-0.1.0-7B68EE?style=flat-square)
![Environment](https://img.shields.io/badge/Environment-Client%20%2B%20Server-5865F2?style=flat-square)
![Status](https://img.shields.io/badge/Status-Active%20Development-yellow?style=flat-square)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

<p align="center">
  <img src="docs/images/origin-selection.jpg" alt="Origins: Reimagined origin selection screen" width="900">
</p>

## Содержание

- [Назначение](#назначение)
- [Совместимость](#совместимость)
- [Архитектура](#архитектура)
- [Отношения мобов](#отношения-мобов)
- [Изменения происхождений](#изменения-происхождений)
  - [Phantom](#phantom)
  - [Elytrian](#elytrian)
  - [Merling](#merling)
  - [Arachnid](#arachnid)
  - [Feline](#feline)
- [Конфигурация](#конфигурация)
- [Управление функциями](#управление-функциями)
- [Теги данных](#теги-данных)
- [Установка](#установка)
- [Сборка](#сборка)
- [Известные ограничения](#известные-ограничения)
- [Отчёты об ошибках](#отчёты-об-ошибках)
- [Лицензия](#лицензия)

## Назначение

Origins: Reimagined не заменяет Origins: Legacy и не хранит выбранное происхождение самостоятельно.

Origins: Legacy остаётся источником истины для:

- активного происхождения игрока;
- стандартных powers;
- синхронизации состояния происхождения;
- назначения происхождения после выбора.

Origins: Reimagined добавляет поверх него:

- собственный клиентский экран выбора;
- presentation- и visual-профили;
- серверные изменения механик отдельных Origins;
- клиентские привязки клавиш с серверной валидацией;
- настраиваемые числовые параметры;
- отдельные переключатели функций для администраторов.

В этом README описаны только изменения, добавленные Origins: Reimagined. Стандартные способности Origins: Legacy намеренно не дублируются.

## Совместимость

| Компонент | Версия разработки | Минимум в `fabric.mod.json` |
|---|---:|---:|
| Minecraft | `26.1.2` | `~26.1` |
| Java | `25` | `25` |
| Fabric Loader | `0.19.3` | `0.18.5` |
| Fabric API | `0.154.2+26.1.2` | `0.145.4` |
| Origins: Legacy | `1.12.15+26.1.2` | `1.12.10` |

Среда выполнения: клиент и сервер (`environment: "*"`).

Для корректной работы игровых механик мод должен быть установлен на сервере. Для нового интерфейса, визуальных профилей и привязок клавиш он также должен быть установлен на клиенте.

## Архитектура

| Слой | Выполняется на | Ответственность |
|---|---|---|
| Origins: Legacy adapter | клиент и сервер | Чтение активных Origins и powers |
| Selection UI | клиент | Каталог Origins, описание, выбор и preview игрока |
| Presentation profiles | клиент и reload ресурсов | Визуальное представление Origins в интерфейсе |
| Visual profiles | клиент | Рендер-модификаторы, anchors и визуальные эффекты |
| Input handlers | клиент | Jump для Elytrian и отдельная клавиша Arachnid |
| Networking | клиент и сервер | Передача запросов без доверия к клиентскому состоянию |
| Gameplay mechanics | сервер | Урон, таймеры, еда, target acquisition и атрибуты |
| Admin feature control | сервер | Отключение отдельных функций без удаления мода |

Клиент отправляет только запрос на использование способности. Сервер повторно проверяет происхождение игрока, состояние функции и игровые условия перед изменением мира или сущности.

## Отношения мобов

Для таблицы ниже используется единая политика:

> **Нейтрален до провокации** — моб не выбирает игрока соответствующего происхождения новой целью. После прямой атаки игроком моб может использовать обычную логику ответной агрессии.

| Origin ID | Entity ID | Политика |
|---|---|---|
| `origins:phantom` | `minecraft:phantom` | Нейтрален до провокации |
| `origins:arachnid` | `minecraft:spider` | Нейтрален до провокации |
| `origins:arachnid` | `minecraft:cave_spider` | Нейтрален до провокации |
| `origins:enderian` | `minecraft:enderman` | Нейтрален до провокации |
| `origins:blazeborn` | `minecraft:blaze` | Нейтрален до провокации |
| `origins:shulk` | `minecraft:shulker` | Нейтрален до провокации |

Условная нейтральность Feline в пустыне описана отдельно, поскольку она зависит от biome tag, текущей цели и временного состояния провокации.

## Изменения происхождений

### Phantom

| Feature ID | Условие | Результат | Значение по умолчанию |
|---|---|---|---:|
| `origins_reimagined:phantom_sunlight_protection` | Игрок имеет `origins:phantom`, находится под открытым небом, снаружи светло, в позиции игрока нет дождя и слот головы не пуст | Tick стандартного `BurnPower` отменяется | Включено |
| `origins_reimagined:phantom_sunlight_protection` | Защита активна и предмет в слоте головы имеет прочность | Предмет получает прямой урон прочности без учёта Unbreaking | `5` прочности каждые `1200` тиков |

Проверка открытого солнца использует одновременно:

- `Level#isBrightOutside()`;
- значение локального освещения выше `0.5`;
- `Level#canSeeSky()`;
- отсутствие дождя в проверяемой позиции.

Недамажимые предметы в слоте головы блокируют солнечный `BurnPower`, но не изнашиваются.

### Elytrian

| Feature ID | Условие | Результат | Значение по умолчанию |
|---|---|---|---:|
| `origins_reimagined:elytrian_flight_cancel` | Игрок имеет `origins:elytrian`, находится в `fall_flying` и нажимает клавишу Jump | Клиент отправляет payload; сервер вызывает `stopFallFlying()` | Jump, обычно `Space` |

Обработчик срабатывает по фронту нажатия: удержание Jump не отправляет запрос каждый tick. Текущий вектор скорости не изменяется, поэтому после отмены полёта игрок продолжает движение и переходит в обычное падение.

### Merling

| Feature ID | Условие | Результат | Значение по умолчанию |
|---|---|---|---:|
| `origins:water_breathing` | Игрок имеет `origins:merling` | Воздух восстанавливается до максимума каждый tick на суше и в воде | Включено |
| `origins_reimagined:merling_underwater_damage` | Merling атакует `LivingEntity`, находясь в воде | Финальный урон атаки умножается на коэффициент | `1.20` |
| `origins_reimagined:merling_fire_damage` | Merling получает урон типа `#minecraft:is_fire` | Получаемый урон умножается на коэффициент | `1.50` |
| `origins_reimagined:merling_forbidden_food` | Используемый предмет находится в `#origins_reimagined:merling_forbidden_food` | Использование завершается с `InteractionResult.FAIL` | Включено |
| `origins_reimagined:merling_nether_desiccation` | Merling находится в Nether дольше grace period | Наносится периодический урон собственного damage type | `1` урона каждые `40` тиков после `200` тиков |

Бонус урона зависит от положения атакующего Merling. Положение цели не учитывается.

`merling_desiccation` является отдельным damage type и не считается обычным огненным уроном. Выход из Nether полностью сбрасывает внутренний таймер.

### Arachnid

| Feature ID | Условие | Результат | Значение по умолчанию |
|---|---|---|---:|
| `origins_reimagined:arachnid_sticky_threads` | Игрок имеет `origins:arachnid` и нажимает клавишу способности | Сервер выполняет entity raycast и создаёт временную паутину в позиции цели | `G`, дальность `32` блока |
| `origins_reimagined:arachnid_sticky_threads` | Паутинная ловушка заканчивается или блок паутины удаляется | Цель получает Slowness I | `40` тиков |
| `origins_reimagined:arachnid_web_speed` | Активная цель ловушки находится не дальше `8` блоков от Arachnid | Скорость Arachnid умножается на коэффициент | `1.10` |

Параметры ловушки:

| Параметр | Значение по умолчанию |
|---|---:|
| Перезарядка | `240` тиков (`12` секунд) |
| Время существования паутины | `30` тиков (`1.5` секунды) |
| Замедление после ловушки | `40` тиков (`2` секунды) |
| Радиус бонуса скорости | `8` блоков |

Перезарядка запускается до проверки попадания, поэтому промах также расходует способность. Паутина создаётся только тогда, когда блок в позиции цели может быть заменён.

### Feline

| Feature ID | Условие | Результат | Значение по умолчанию |
|---|---|---|---:|
| `origins_reimagined:feline_desert_neutrality` | Игрок имеет `origins:feline` и находится в `#origins_reimagined:desert_biomes` | `Mob` не может выбрать игрока новой целью | Включено |
| `origins_reimagined:feline_wet_wool` | Feline находится в воде или не истёк wet grace period | Скорость передвижения уменьшается; sprint jump возвращается к базовой силе | `0.75` скорости, `80` тиков grace |
| `origins_reimagined:feline_food_bonus` | Feline съедает предмет из `#minecraft:meat` или `#minecraft:fishes` | Увеличивается только полученное насыщение | `1.20` |

Детали пустынной нейтральности:

- блокируется только назначение новой цели;
- моб, уже преследующий Feline до входа в пустыню, не сбрасывает текущую цель;
- прямой удар Feline провоцирует конкретного моба на `1200` тиков (`60` секунд);
- провокация хранится отдельно для каждой пары моб–игрок;
- по умолчанию desert tag содержит только `minecraft:desert`.

Детали мокрой шерсти:

- дождь сам по себе не активирует штраф;
- лодка отключает штраф и очищает wet state;
- после выхода из воды штраф сохраняется ещё `80` тиков;
- обычный прыжок не отключается, но усиленный sprint jump заменяется базовым.

Бонус еды не изменяет количество восстанавливаемых очков голода.

## Конфигурация

Основные числовые параметры хранятся в:

```text
config/origins_reimagined.properties
```

| Ключ | Значение по умолчанию | Назначение |
|---|---:|---|
| `merling.underwater_damage_multiplier` | `1.20` | Множитель урона Merling в воде |
| `merling.fire_damage_multiplier` | `1.50` | Множитель входящего огненного урона |
| `merling.nether_grace_ticks` | `200` | Задержка до высыхания в Nether |
| `merling.nether_damage_interval_ticks` | `40` | Интервал урона от высыхания |
| `merling.nether_damage` | `1.0` | Урон за один tick интервала |
| `arachnid.sticky_cooldown_ticks` | `240` | Перезарядка выстрела паутиной |
| `arachnid.web_duration_ticks` | `30` | Время существования временной паутины |
| `arachnid.slow_duration_ticks` | `40` | Длительность Slowness после ловушки |
| `arachnid.speed_multiplier` | `1.10` | Множитель скорости рядом с активной целью |
| `feline.food_saturation_multiplier` | `1.20` | Множитель полученного насыщения |
| `feline.wet_speed_multiplier` | `0.75` | Множитель скорости при намокании |
| `feline.wet_grace_ticks` | `80` | Длительность штрафа после выхода из воды |

Файл загружается сервером. Изменения, внесённые через административный интерфейс, сохраняются в этот файл и применяются повторной загрузкой конфигурации.

## Управление функциями

Отдельные механики можно отключать без удаления мода. Все отсутствующие overrides считаются включёнными.

Файл overrides:

```text
config/origins_reimagined_features.properties
```

Команды доступны операторам сервера:

```text
/originsreimagined admin ui
/originsreimagined admin feature list
/originsreimagined admin feature set <origin_id> <feature_id> <true|false>
/originsreimagined admin feature reset <origin_id>
/originsreimagined admin feature reset <origin_id> <feature_id>
```

Пример:

```text
/originsreimagined admin feature set origins:merling origins_reimagined:merling_fire_damage false
```

`list` выводит только функции, явно отключённые администратором.

## Теги данных

| Tag ID | Использование | Значения по умолчанию |
|---|---|---|
| `#origins_reimagined:desert_biomes` | Биомы для пустынной нейтральности Feline | `minecraft:desert` |
| `#origins_reimagined:merling_forbidden_food` | Еда, запрещённая Merling | cod, cooked cod, salmon, cooked salmon, tropical fish, pufferfish |
| `#minecraft:meat` | Дополнительное насыщение Feline | Определяется Minecraft и datapacks |
| `#minecraft:fishes` | Дополнительное насыщение Feline | Определяется Minecraft и datapacks |

Теги используют `replace: false`, поэтому datapack может расширять списки без замены встроенных значений.

## Установка

Установи на сервер и все клиенты:

```text
fabric-api-*.jar
origins-legacy-*.jar
origins-reimagined-*.jar
```

Каталоги:

```text
.minecraft/mods
server/mods
```

Перед обновлением серверной сборки останови сервер и сделай резервную копию мира и каталога `config`.

## Сборка

### Linux / macOS

```bash
git clone https://github.com/AndrewImm-OP/origins-reimagined.git
cd origins-reimagined
chmod +x gradlew
./gradlew clean build
```

### Windows PowerShell

```powershell
git clone https://github.com/AndrewImm-OP/origins-reimagined.git
cd origins-reimagined
.\gradlew.bat clean build
```

Результат:

```text
build/libs/
```

Для сборки требуется JDK 25.

## Известные ограничения

- Проект разрабатывается и тестируется для Minecraft `26.1.2`.
- Числовой баланс может изменяться до стабильного релиза.
- Клиент без мода не получает новый экран, visual profiles и привязки клавиш.
- Моды, изменяющие те же методы через mixin, могут конфликтовать на уровне injection points.
- Datapacks, заменяющие стандартные Origins или powers, могут изменить ожидаемое поведение.
- Временные runtime-состояния ловушек, провокаций и таймеров не предназначены для постоянного хранения между перезапусками сервера.

## Отчёты об ошибках

Перед созданием issue проверь:

1. Minecraft имеет версию `26.1.2`.
2. Клиент и сервер используют одинаковые версии мода.
3. Установлены Fabric API и Origins: Legacy.
4. Ошибка повторяется без других аддонов, изменяющих те же Origins.
5. Конфигурационные файлы не содержат случайно изменённых значений.

Добавь в отчёт:

- `latest.log`;
- crash report, если он создан;
- список модов и их версии;
- версию Java;
- выбранный Origin ID;
- одиночную или серверную среду;
- точные шаги воспроизведения;
- ожидаемый и фактический результат.

Пример:

```text
Origin: origins:arachnid
Environment: dedicated server
Action: press Sticky Threads while aiming at a player
Expected: temporary cobweb and cooldown
Actual: payload sent, but no cobweb is created
```

## Лицензия

Исходный код распространяется по лицензии [MIT](LICENSE).

Origins: Reimagined является независимым аддоном и не является официальной частью Mojang Studios, Origins или Origins: Legacy. Названия и торговые марки принадлежат соответствующим правообладателям.
