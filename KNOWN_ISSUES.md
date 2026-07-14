# Origins Overhaul — известные ограничения и риски

## Среда аудита

- В `/home/andrew/projects` исходный проект Origins Overhaul отсутствует; документы создаются как стартовый каркас.
- Upstream Origins Legacy доступен как ветка `versions/26.1`; отдельного публичного тега `26.1.2` в GitHub-репозитории не обнаружено. Версия `26.1.2` подтверждается опубликованным артефактом и Gradle properties upstream.
- Клиентский `runClient` в текущем headless-окружении не дошёл до окна Minecraft за 30 секунд; нужна графическая сессия и/или более длинный timeout для полноценной проверки.
- Новый экран также не прошёл визуальный runtime-тест по той же причине; `Client launch` намеренно остаётся `BLOCKED`.
- Dedicated server dev-run требует стандартный `eula.txt`; в проверке Fabric и оба common entrypoints загрузились, но мир не запускался из-за отсутствия согласия с EULA.

## API-риски

- `OriginRegistry`, `OriginLayers` и `PowerTypeRegistry` — обычные статические map-based API, а не vanilla registries. Доступ к ним нужно изолировать адаптером, чтобы пережить datapack reload и client/server lifecycle.
- Список origin IDs в layer может содержать условные ветви (`ConditionedOrigin`); допустимость нужно получать через `OriginLayer.getOrigins(player)`, а не только через `getOrigins()`.
- `Origin.getPowerTypes()` возвращает только зарегистрированные `PowerType`; неизвестный power при загрузке origin логируется и пропускается. Fallback должен сохранять origin без crash и явно учитывать потерю описания.
- `PowerType.isHidden()` — единственный штатный флаг скрытия power. Для расширенного UI потребуется собственная policy/profile-слой поверх него.
- Серверная проверка выбора уже есть в `ModPacketsC2S.chooseOrigin`: проверяются layer, `isChoosable()` и `layer.contains(origin, player)`. Новый пакет не должен обходить эту проверку.

## Экран выбора

- Для перехода между несколькими слоями используется совместимый subclass upstream `WaitForNextLayerScreen`; визуальная компоновка и поиск не проверены в живом окне.
- Кнопка случайного выбора отображается как текстовый control рядом с подтверждением; её расположение следует проверить на крайних GUI Scale.
- Анимации не прошли визуальную проверку в настоящем окне Minecraft из-за headless-окружения; graphical runtime остаётся `BLOCKED`.
- Поворот ItemStack выполняется через текущий GUI pose stack и требует визуальной проверки на разных ресурс-паках.
- Player preview использует штатный `GuiGraphicsExtractor.skin` и поэтому намеренно не отображает armor, held items, cape или elytra geometry; соответствующие snapshot texture IDs сохранены для будущего renderer pipeline.
- `preview_show_equipment` и `preview_show_cape` пока сохраняются как настройки API, но не включают неподдерживаемую GUI geometry.
- Classic/slim и clipping проверены только компиляцией и isolated math tests; живое окно Minecraft недоступно.

## Поведение мобов

Apoli Legacy уже имеет `ModifyBehaviorPower` и mixin-точки в `Mob`, `TargetingConditions` и `NearestAttackableTargetGoal`, но это поведение ограничено power-источниками игрока и не покрывает retaliation, случайный урон, Thorns, питомцев или смену origin. Relation engine должен быть отдельным фильтром с явным порядком приоритетов.

## Phantom

`origins:burn_in_daylight` — это `origins:burn` с интервалом 20, а `BurnPower.tick()` только периодически устанавливает fire ticks. `origins:exposed_to_sun` использует одну округлённую позицию, `getLightLevelDependentMagicValue()` и `canSeeSky()`. В upstream нет exposure-счётчика, учёта head ItemStack, sample points, underwater suppression или отдельного солнечного damage state.
