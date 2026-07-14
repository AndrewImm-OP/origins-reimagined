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

## Visual profiles

- Graphical validation of visual profiles is blocked by the headless environment.
- The current preview backend applies the shared model tint through `GuiSkinRenderer`; it does not yet render arbitrary geometry attachments, particles, armor or true fullbright emissive layers.
- World texture/emissive modifiers use a safe cutout render pass. `model_alpha` is parsed and conditionally resolved, but transparent whole-player rendering is not enabled until depth/blend ordering is validated in a real client.
- The minimal eye editor stores anchors and presets, but eye texture overlays are not yet rendered by the GUI backend.
- Custom skin anchor identity currently hashes the skin texture identifier, not downloaded PNG bytes.
- Attachments currently bake cuboid-like geometry; `plane`, `cross_planes` and `segmented_chain` are accepted definitions with conservative cuboid fallback, not specialized mesh topology.
- Preview particles are not emitted yet. World particle aura is client-only and rate-limited.
- Whole-model alpha is capability-disabled until a real client confirms depth/blend ordering with outer layers and Iris.
- Built-in texture references use vanilla resource identifiers where possible; final custom art assets still require graphical review.
- Visual Test Lab currently exposes keyboard controls and a compact diagnostics view; attachment bounds, axes, UV grid and lighting presets are not yet implemented.
- The experimental alpha toggle is not exposed until depth/blend behavior is validated in a graphical client.
- The agent cannot execute the required graphical smoke-test because `runClient` stops during headless launch preparation.

## Поведение мобов

Apoli Legacy уже имеет `ModifyBehaviorPower` и mixin-точки в `Mob`, `TargetingConditions` и `NearestAttackableTargetGoal`, но это поведение ограничено power-источниками игрока и не покрывает retaliation, случайный урон, Thorns, питомцев или смену origin. Relation engine должен быть отдельным фильтром с явным порядком приоритетов.

## Phantom

`origins:burn_in_daylight` — это `origins:burn` с интервалом 20, а `BurnPower.tick()` только периодически устанавливает fire ticks. `origins:exposed_to_sun` использует одну округлённую позицию, `getLightLevelDependentMagicValue()` и `canSeeSky()`. В upstream нет exposure-счётчика, учёта head ItemStack, sample points, underwater suppression или отдельного солнечного damage state.
