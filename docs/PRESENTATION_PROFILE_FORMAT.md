# Presentation profile format

Файлы загружаются из `data/<namespace>/origins_overhaul/presentations/*.json`. Профиль не изменяет origin, его powers или доступность — только собирает presentation snapshot.

```json
{
  "origin": "origins:phantom",
  "difficulty": {"use_impact": true, "color": "#D75CFF"},
  "advantages": [{
    "powers": ["origins:phantomize", "origins:phasing"],
    "name": {"translate": "example.phantom.form"},
    "description": {"translate": "example.phantom.form.description"},
    "order": 0
  }],
  "disadvantages": [],
  "neutral_features": [],
  "hidden_powers": ["origins:phantomize_overlay"],
  "visual_profile": "origins_overhaul:phantom",
  "relation_profile": "origins_overhaul:phantom"
}
```

`powers` может содержать один или несколько power IDs. Отсутствующие powers дают warning и не ломают fallback. Все реальные powers, которые не были использованы профилем и не указаны в `hidden_powers`, попадают в `NEUTRAL`. Нераспознанный или повреждённый JSON пропускается.

Поддерживаются `translate` и `text` для name/description. Цвет принимает 6- или 8-значный HEX с `#`; неверный цвет заменяется цветом impact.

Для отладочной пересборки включить JVM-флаг `-Dorigins_overhaul.debug=true`. Catalog пишет только агрегаты: число layers, origins и время rebuild.
