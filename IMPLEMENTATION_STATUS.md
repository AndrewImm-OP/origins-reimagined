# Origins Overhaul — статус реализации

Дата аудита: 2026-07-14

## Текущий этап

Этап 2 — рабочий каркас и универсальная модель Origins.

- [x] Найден upstream: `BluSpring/Origins-Legacy`, ветка `versions/26.1`.
- [x] Проверены Minecraft/Fabric/Java параметры.
- [x] Проверены origin/layer loaders и клиентская синхронизация.
- [x] Проверены стандартные экраны выбора и просмотра.
- [x] Проверены power registry, hidden powers и network payloads.
- [x] Проверены текущие точки AI-отношений.
- [x] Проверена текущая реализация Phantom daylight.
- [x] Создан исходный мод Origins Overhaul.
- [x] Реализован универсальный fallback.
- [ ] Реализован новый экран.
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

## Статус сборки исследуемого upstream

JDK 25 подтверждён: `openjdk version "25.0.3"`. Проект собирается через локальный `--gradle-user-home .gradle-user-home`. Dedicated server прошёл Fabric/Origins entrypoint initialization без client classloading errors, затем остановился на стандартном требовании EULA. `runClient` дошёл до подготовки запуска, но в headless-окружении истёк 30-секундный timeout во время asset/setup phase; полноценная графическая проверка не выполнена.

## Следующий этап

Создать отдельный Fabric-проект Origins Overhaul с зависимостью на Origins Legacy, не изменяя upstream. Первым функциональным срезом сделать только серверно-клиентскую модель данных и fallback presentation.
