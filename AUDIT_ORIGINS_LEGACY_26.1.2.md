# Аудит Origins Legacy 26.1.2

## Проверенная база

- Репозиторий: `BluSpring/Origins-Legacy`.
- Ветка: `versions/26.1`.
- Upstream: Minecraft `26.1.2`, Fabric Loader `0.18.5`, Fabric API `0.145.4+26.1.2`, Java release `25`.
- Встроенные submodules: `BluSpring/apoli-legacy` и `BluSpring/calio-legacy`, ветки `versions/26.1`.

## Origins и layers

`io.github.apace100.origins.origin.Origin` загружается `OriginManager extends MultiJsonDataLoader` из data path `origins` и хранится в map-based `OriginRegistry`.

Presentation API origin:

- `getIdentifier()`;
- `getDisplayItem()`;
- `getImpact()` / `Impact.getImpactValue()`;
- `getOrder()`;
- `isChoosable()`;
- `getPowerTypes()` / `getPowerIds()`;
- `getName()` / `getDescription()`;
- `isSpecial()` и `getUpgrades()`.

Origin JSON поддерживает `powers`, `icon`, `unchoosable`, `order`, `impact`, `loading_priority`, `upgrades`, `name`, `description`. Неизвестный power пропускается с ошибкой в лог; origin не удаляется.

`io.github.apace100.origins.origin.OriginLayer` загружается `OriginLayers extends MultiJsonDataLoader` из data path `origin_layers` и хранится в map `OriginLayers`.

Layer API:

- `getOrigins(Player)` применяет conditioned branches и фильтрует отсутствующие IDs;
- `getOriginOptionCount(Player)` учитывает choosable origins и random option;
- `isEnabled()`, `isHidden()`, `isRandomAllowed()`;
- default origin и auto-choice;
- `order` для последовательности layers.

Поддерживаются `order`, `enabled`, `origins`, `name`, `gui_title`, `missing_name`, `missing_description`, `allow_random`, `allow_random_unchoosable`, `exclude_random`, `default_origin`, `auto_choose`, `hidden`.

`OriginComponent.checkAutoChoosingLayers()` проходит enabled layers по order, поэтому проект обязан поддерживать несколько слоёв и последовательный выбор.

## Player state

`OriginComponent`/`PlayerOriginComponent` хранят `HashMap<OriginLayer, Origin>`. `setOrigin()` добавляет powers с source = origin ID, удаляет powers старого source и вызывает origin criterion. Компонент синхронизируется через Cardinal Components API.

Preview нельзя делать через `setOrigin()` на реальном игроке: это меняет gameplay powers, callbacks и сохраняемое состояние. Нужен независимый `PreviewOriginContext`.

## Экраны и открытие

`LoginMixin.openOriginsGui()` после `PlayerList.placeNewPlayer` отправляет `PowersAndOriginsPacket`, `LayerListPacket`, badges и `OpenOriginScreenPacket`. Клиент `ModPacketsS2C.openOriginScreen()` собирает незавершённые enabled layers, сортирует их и создаёт `ChooseOriginScreen`.

Текущие классы:

- `OriginDisplayScreen` — vanilla-style окно, dirt/background option, один scrollbar, имя/icon/impact, описание и линейный список visible powers;
- `ChooseOriginScreen` — origins одного layer, сортировка impact/order, стандартные Buttons, random и Select;
- `WaitForNextLayerScreen` — переход между layers;
- `ViewOriginScreen` — просмотр выбранных origins.

Точка замены — клиентская обработка `OpenOriginScreenPacket`, не серверный component.

## Powers

`io.github.apace100.apoli.power.PowerType` и `PowerTypeRegistry` дают ID, translation keys, description и `isHidden()`. Реализация/factory содержит сериализованные данные, но не является стабильным UI-контрактом.

`MultiplePowerType` может содержать sub-powers. `Origin.hasPowerType()` учитывает их, поэтому профиль должен поддерживать объединение технических IDs. Для неизвестных типов безопасная категория — `ОСОБЕННОСТИ`; нельзя привязывать fallback к Java-классам аддонов.

Текущая vanilla-like отрисовка `OriginDisplayScreen` пропускает только `p.isHidden()`, затем показывает `p.getName()` и `p.getDescription()`. Это место не подходит для автоматического определения advantages/disadvantages без отдельной policy/profile модели.

## Network contract

C2S: `ChooseOriginPacket(originId, layerId)` и `ChooseRandomOriginPacket(layerId)`.

S2C: `OpenOriginScreenPacket`, `OriginListPacket`, `LayerListPacket`, `ConfirmOriginPacket`, `PowersAndOriginsPacket`.

`ModPacketsC2S.chooseOrigin()` повторно получает layer/origin на сервере и проверяет `isChoosable()` и `layer.contains(origin, player)`. Клиентский список не является источником истины. Новые presentation/visual payloads должны иметь отдельную protocol version, а gameplay selection — оставаться серверным.

## Mob relations

Apoli Legacy уже применяет `ModifyBehaviorPower` через:

- `apoli.mixin.legacy.MobMixin` — custom target goal и удаление passive target;
- `apoli.mixin.legacy.TargetingConditionsMixin` — запрет combat targeting passive;
- `apoli.mixin.legacy.NearestAttackableTargetGoalMixin` — фильтрация найденной цели, если behavior не hostile.

Этого недостаточно для relation engine: отсутствуют retaliation timer, damage-source policy, питомцы, Thorns, случайный урон и смена origin во время боя. Новую систему нужно фильтровать по relation profile и entity tags, не делать безусловный глобальный mixin.

## Phantom daylight

`origins:burn_in_daylight.json` — `origins:burn` с `interval: 20`, `burn_duration: 6`, condition `exposed_to_sun` и not invisible. `BurnPower.tick()` только периодически вызывает `entity.setRemainingFireTicks()`.

`origins:exposed_to_sun` проверяет bright outside, rain, одну округлённую позицию, light value и `level.canSeeSky(blockPos)`. В upstream отсутствуют exposure state, несколько sample points, головной ItemStack, wear interval, underwater suppression и отдельный solar damage state. Это подтверждает необходимость отдельной `PhantomSunState`, а не изменения PNG или обычного fire state.

## Визуальные точки

Origins Legacy не предоставляет универсальный origin render-layer API. Apoli имеет собственные power/render hooks, но косметические слои Origins: Reimagined следует держать отдельно и отключать/понижать до safe mode при обнаружении полной замены player model сторонним модом.

## Рекомендованный первый срез

1. Создать `OriginsLegacyAdapter` в common для registry/layer/power/component доступа.
2. После resource reload собирать immutable presentation snapshot.
3. При login принимать серверный snapshot и не доверять client-only профилям для gameplay.
4. Реализовать fallback presentation до встроенных профилей.
5. Затем заменять экран, сохраняя штатные server-side selection checks.
