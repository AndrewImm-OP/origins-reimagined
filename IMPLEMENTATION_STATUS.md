# Origins Overhaul — статус реализации

Дата аудита: 2026-07-14

## Текущий этап

Этап 6.1 — завершение визуального рендера.

- [x] Найден upstream: `BluSpring/Origins-Legacy`, ветка `versions/26.1`.
- [x] Проверены Minecraft/Fabric/Java параметры.
- [x] Проверены origin/layer loaders и клиентская синхронизация.
- [x] Проверены стандартные экраны выбора и просмотра.
- [x] Проверены power registry, hidden powers и network payloads.
- [x] Проверены текущие точки AI-отношений.
- [x] Проверена текущая реализация Phantom daylight.
- [x] Создан исходный мод Origins Overhaul.
- [x] Реализован универсальный fallback.
- [x] Реализованы статичный экран выбора и frame-independent анимации; runtime-открытие ожидает графическую проверку.
- [x] Реализован isolated player skin preview; graphical runtime ожидает проверки.
- [x] Реализована общая модель visual profiles, resolver и client reload loader.
- [x] Реализованы безопасные preview/world tint passes и fallback.
- [ ] Реализованы полноценные attachments, particles, true emissive и alpha passes.
- [ ] Реализован relation engine.
- [ ] Реализован PhantomSunState.

| Область | Статус |
|---|---|
| Environment | COMPLETE |
| Project scaffold | COMPLETE |
| Origins Legacy dependency | COMPLETE |
| Common entrypoint | COMPLETE |
| Client entrypoint | COMPLETE |
| Origins Legacy adapter | COMPLETE |
| Client catalog | COMPLETE |
| JSON profile loader | COMPLETE |
| Fallback presentation | COMPLETE |
| Human profile | COMPLETE |
| Phantom profile | COMPLETE |
| Debug inspection | COMPLETE |
| Test datapack | COMPLETE |
| Client launch | BLOCKED |
| Dedicated server launch | COMPLETE |
| Build | COMPLETE |
| Animation clock | COMPLETE |
| Animation state machine | COMPLETE |
| Screen entrance | COMPLETE |
| Background fade | COMPLETE |
| Header transition | COMPLETE |
| Icon rotation | COMPLETE |
| Text reveal | COMPLETE |
| Ability stagger | COMPLETE |
| Origin switch transition | COMPLETE |
| Rapid input handling | COMPLETE |
| Search overlay transition | COMPLETE |
| Reduce motion | COMPLETE |
| Animation configuration | COMPLETE |
| Animation unit tests | COMPLETE |
| Graphical runtime validation | BLOCKED |
| Visual rendering audit | COMPLETE |
| Visual profile loader | COMPLETE |
| Visual profile resolver | COMPLETE |
| Preview visual backend | COMPLETE |
| World visual backend | COMPLETE |
| Texture overlays | IN PROGRESS |
| Emissive overlays | IN PROGRESS |
| Model tint | COMPLETE |
| Model alpha | BLOCKED |
| Geometry attachments | NOT STARTED |
| Particle aura | NOT STARTED |
| Model-part anchors | COMPLETE |
| Eye anchors | COMPLETE |
| Skin hash | IN PROGRESS |
| Eye anchor editor | IN PROGRESS |
| Preview states | COMPLETE |
| Human profile | COMPLETE |
| Phantom profile | COMPLETE |
| Enderian profile | COMPLETE |
| Arachnid profile | COMPLETE |
| Blazeborn profile | COMPLETE |
| Merling profile | COMPLETE |
| Feline profile | COMPLETE |
| Avian profile | COMPLETE |
| Elytrian profile | COMPLETE |
| Shulk profile | COMPLETE |
| Other player rendering | COMPLETE |
| Visual preferences | COMPLETE |
| Visual cache | NOT STARTED |
| Visual tests | IN PROGRESS |
| Render capabilities | COMPLETE |
| Real texture overlay pass | COMPLETE |
| Eye overlay pass | COMPLETE |
| Emissive render pass | COMPLETE |
| Baked cuboid attachments | COMPLETE |
| Segmented attachment definition | IN PROGRESS |
| World particle aura | COMPLETE |
| Preview particle aura | NOT STARTED |
| Model alpha validation | BLOCKED |
| Phantom reference profile | IN PROGRESS |
| Enderian reference profile | COMPLETE |
| Feline reference profile | COMPLETE |
| Merling reference profile | COMPLETE |
| Visual Test Lab | COMPLETE |
| Profile selector | COMPLETE |
| Model type override | COMPLETE |
| State simulation | COMPLETE |
| Capability controls | COMPLETE |
| Attachment bounds | NOT STARTED |
| Anchor debug | NOT STARTED |
| UV debug | NOT STARTED |
| Lighting debug | NOT STARTED |
| Armor test flags | NOT STARTED |
| True segmented chain | COMPLETE |
| Segmented animation | COMPLETE |
| Preview particles | COMPLETE |
| Experimental model alpha | BLOCKED |
| World debug override | IN PROGRESS |
| Diagnostics export | COMPLETE |
| Local graphical smoke-test | BLOCKED |
| Sodium validation | BLOCKED |
| Iris validation | BLOCKED |
| Slim validation | BLOCKED |
| Armor interaction validation | BLOCKED |
| Screen interception | COMPLETE |
| Selection session | COMPLETE |
| Background | COMPLETE |
| Header | COMPLETE |
| Advantages column | COMPLETE |
| Disadvantages column | COMPLETE |
| Neutral features | COMPLETE |
| Origin navigation | COMPLETE |
| Origin list/search | COMPLETE |
| Multiple layers | COMPLETE |
| Random selection | COMPLETE |
| Standard payload submission | COMPLETE |
| Localization | COMPLETE |
| Client runtime test | BLOCKED |
| Elytrian flight cancellation | COMPLETE |
| Dedicated server test | COMPLETE |
| Player rendering audit | COMPLETE |
| Preview architecture | COMPLETE |
| Appearance snapshot | COMPLETE |
| Skin loading | COMPLETE |
| Classic model | COMPLETE |
| Slim model | COMPLETE |
| Preview camera | COMPLETE |
| Mouse rotation | COMPLETE |
| Zoom | COMPLETE |
| Pan | COMPLETE |
| Reset | COMPLETE |
| Auto rotation | COMPLETE |
| Outer skin layer | COMPLETE |
| Equipment visibility | BLOCKED |
| Preview clipping | COMPLETE |
| Animation integration | COMPLETE |
| Lifecycle cleanup | COMPLETE |
| Preview tests | COMPLETE |
| Graphical runtime validation | BLOCKED |

## Статус сборки исследуемого upstream

JDK 25 подтверждён: `openjdk version "25.0.3"`. Проект собирается через локальный `--gradle-user-home .gradle-user-home`. Dedicated server прошёл Fabric/Origins entrypoint initialization без client classloading errors, затем остановился на стандартном требовании EULA. `runClient` дошёл до подготовки запуска, но в headless-окружении истёк 30-секундный timeout во время asset/setup phase; полноценная графическая проверка не выполнена.

## Ограничение проверки этапа

Экран компилируется, но не был реально открыт в Minecraft: текущая среда headless, поэтому `Client launch` и `Client runtime test` остаются `BLOCKED`. Компиляция клиента и dedicated-server classloading проверены.

## Следующий этап

Добавить visual profiles и подключить их к `PreviewOriginContext`, не меняя selection protocol.
