# Админские настройки Origins: Reimagined

Игровые механики выполняются сервером. Клиентский интерфейс только отправляет запрос на изменение, а сервер проверяет права оператора и валидирует значение.

## UI

Оператор открывает интерфейс командой:

```text
/originsreimagined admin ui
```

Интерфейс выполнен в стиле контейнера Minecraft: слева выбирается происхождение, справа отображаются зарегистрированные числовые параметры, а кнопки `−` и `+` отправляют изменение на сервер.

## Feature-gates

```text
/originsreimagined admin feature set <origin> <feature> false
/originsreimagined admin feature set <origin> <feature> true
/originsreimagined admin feature reset <origin>
/originsreimagined admin feature reset <origin> <feature>
/originsreimagined admin feature list
```

Отсутствующая запись считается включённой. Настройки сохраняются в `config/origins_reimagined_features.properties` и действуют только на сервере.

## Числовые параметры

Числовые настройки хранятся в `config/origins_reimagined.properties`. Серверные значения, изменяемые UI, включают:

```properties
arachnid.sticky_cooldown_ticks=240
arachnid.web_duration_ticks=30
arachnid.slow_duration_ticks=40
arachnid.speed_multiplier=1.10
feline.food_saturation_multiplier=1.20
feline.wet_speed_multiplier=0.75
feline.wet_grace_ticks=80
```

Тиковые значения не могут быть меньше одного, множители не принимают отрицательные или нечисловые значения. Все остальные Origin powers не получают новых числовых значений автоматически: их feature-gates управляются отдельно по идентификатору power, чтобы не подменять серверную логику Origins Legacy.
