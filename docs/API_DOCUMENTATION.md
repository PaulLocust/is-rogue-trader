# API Документация - Rogue Trader System

## Обзор

REST API для информационной системы управления вольными торговцами. Система реализует управление планетами, событиями, проектами, сообщениями и ресурсами империи.

## Swagger UI

После запуска приложения документация API доступна по адресу:
- **Swagger UI**: http://localhost:40000/swagger-ui.html
- **OpenAPI JSON**: http://localhost:40000/api-docs

## Использование PL/pgSQL функций

Система использует следующие PL/pgSQL функции из базы данных:

1. **send_message()** - используется в `MessageService.sendMessage()`
   - Отправляет сообщение с возможностью искажения для астропатов
   - Автоматически применяет искажение в зависимости от psi_level астропата

2. **resolve_crisis()** - используется в `EventService.resolveCrisis()`
   - Разрешает кризис с помощью ресурсов (HELP) или игнорирует его (IGNORE)
   - Автоматически обновляет лояльность и ресурсы планеты

3. **get_empire_resources()** - используется в `EmpireService.getEmpireResources()`
   - Рассчитывает общие ресурсы империи торговца
   - Учитывает только небунтующие планеты

## Основные эндпоинты

### Торговцы (`/api/traders`)

- `GET /api/traders/{id}` - Получить торговца по ID
- `GET /api/traders/{id}/planets` - Получить планеты торговца
- `PUT /api/traders/{id}/influence` - Обновить влияние торговца

### Планеты (`/api/planets`)

- `GET /api/planets/{id}` - Получить планету по ID
- `GET /api/planets/trader/{traderId}` - Получить планеты торговца
- `GET /api/planets/trader/{traderId}/rebellious` - Получить бунтующие планеты
- `PUT /api/planets/{id}/loyalty` - Обновить лояльность планеты

### Сообщения (`/api/messages`) - использует PL/pgSQL

- `POST /api/messages` - Отправить сообщение (использует `send_message()`)
- `GET /api/messages/user/{userId}` - Получить сообщения пользователя
- `GET /api/messages/{id}` - Получить сообщение по ID

**Пример запроса на отправку сообщения:**
```json
{
  "senderId": 2,
  "receiverId": 1,
  "content": "Всё спокойно на Аграрий-Прима",
  "distortionChance": 0.1
}
```

### События (`/api/events`) - использует PL/pgSQL

- `GET /api/events/planet/{planetId}` - Получить события планеты
- `GET /api/events/planet/{planetId}/active` - Получить активные события
- `GET /api/events/{id}` - Получить событие по ID
- `POST /api/events` - Создать событие
- `POST /api/events/{id}/resolve` - Разрешить кризис (использует `resolve_crisis()`)

**Пример запроса на разрешение кризиса:**
```json
{
  "action": "HELP",
  "wealth": 2000,
  "industry": 1000
}
```

### Проекты (`/api/projects`)

- `POST /api/projects` - Создать проект (триггер автоматически проверяет ресурсы)
- `GET /api/projects/planet/{planetId}` - Получить проекты планеты
- `GET /api/projects/{id}` - Получить проект по ID
- `PUT /api/projects/{id}/status` - Обновить статус проекта

**Пример запроса на создание проекта:**
```json
{
  "planetId": 1,
  "upgradeId": 1
}
```

### Империя (`/api/empire`) - использует PL/pgSQL

- `GET /api/empire/{traderId}/resources` - Получить ресурсы империи (использует `get_empire_resources()`)
- `GET /api/empire/{traderId}/influence` - Получить влияние торговца

### Маршруты (`/api/routes`)

- `POST /api/routes` - Создать маршрут
- `GET /api/routes/navigator/{navigatorId}` - Получить маршруты навигатора
- `GET /api/routes/{id}` - Получить маршрут по ID
- `GET /api/routes/{id}/stability` - Проверить стабильность маршрута

## Триггеры базы данных

Система использует следующие триггеры:

1. **rebellion_check** - автоматически обновляет статус мятежа при изменении лояльности планеты
2. **resource_check** - автоматически проверяет и списывает ресурсы при создании проекта со статусом PLANNED

## Архитектура

Система построена по трехслойной архитектуре:

1. **Presentation Layer** - REST контроллеры с Swagger документацией
2. **Business Logic Layer** - Сервисы, использующие PL/pgSQL функции
3. **Data Access Layer** - JPA репозитории и сущности

Подробная диаграмма классов находится в файле `docs/class_diagram.md`.

