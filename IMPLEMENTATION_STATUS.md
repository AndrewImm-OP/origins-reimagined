# Origins Overhaul — статус реализации

Дата аудита: 2026-07-14

## Текущий этап

Этап 3 — функциональный статичный экран выбора происхождения.

- [x] Найден upstream: `BluSpring/Origins-Legacy`, ветка `versions/26.1`.
- [x] Проверены Minecraft/Fabric/Java параметры.
- [x] Проверены origin/layer loaders и клиентская синхронизация.
- [x] Проверены стандартные экраны выбора и просмотра.
- [x] Проверены power registry, hidden powers и network payloads.
- [x] Проверены текущие точки AI-отношений.
- [x] Проверена текущая реализация Phantom daylight.
- [x] Создан исходный мод Origins Overhaul.
- [x] Реализован универсальный fallback.
- [x] Реализован статичный функциональный экран выбора; runtime-открытие ожидает графическую проверку.
- [ ] Реализованы визуальные профили.
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
| Dedicated server test | COMPLETE |

## Статус сборки исследуемого upstream

JDK 25 подтверждён: `openjdk version "25.0.3"`. Проект собирается через локальный `--gradle-user-home .gradle-user-home`. Dedicated server прошёл Fabric/Origins entrypoint initialization без client classloading errors, затем остановился на стандартном требовании EULA. `runClient` дошёл до подготовки запуска, но в headless-окружении истёк 30-секундный timeout во время asset/setup phase; полноценная графическая проверка не выполнена.

## Ограничение проверки этапа

Экран компилируется, но не был реально открыт в Minecraft: текущая среда headless, поэтому `Client launch` и `Client runtime test` остаются `BLOCKED`. Компиляция клиента и dedicated-server classloading проверены.

## Следующий этап

Добавить статические анимации переходов и preview pipeline игрока, не меняя selection protocol.
