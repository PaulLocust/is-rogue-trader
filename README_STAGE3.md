# Этап 3 - Реализация уровня хранения и бизнес-логики

## Выполненные задачи

### 1. Диаграмма классов
Создана диаграмма классов, представляющая общую архитектуру системы. Файл: `docs/class_diagram.md`

Архитектура включает:
- **Presentation Layer** - REST контроллеры
- **Business Logic Layer** - Сервисы
- **Data Access Layer** - Репозитории
- **Entity Layer** - JPA сущности
- **Database Layer** - PostgreSQL + PL/pgSQL функции

### 2. Уровень хранения данных (Data Access Layer)

Реализованы:
- **JPA сущности** для всех таблиц БД:
  - User, RogueTrader, Planet, Message, Event, Project, Route, Upgrade
  - Governor, Astropath, Navigator
- **Репозитории** (JpaRepository) для всех сущностей с дополнительными методами запросов
- **Перечисления** (enums): UserRole, PlanetType, EventType, ProjectStatus

### 3. Уровень бизнес-логики (Business Logic Layer)

Реализованы сервисы, которые **используют PL/pgSQL функции** из этапа 2:

#### MessageService
- `sendMessage()` - использует PL/pgSQL функцию `send_message()`
- Отправляет сообщения с возможностью искажения для астропатов

#### EventService
- `resolveCrisis()` - использует PL/pgSQL функцию `resolve_crisis()`
- Разрешает кризисы с помощью ресурсов (HELP) или игнорирует их (IGNORE)

#### EmpireService
- `getEmpireResources()` - использует PL/pgSQL функцию `get_empire_resources()`
- Рассчитывает общие ресурсы империи торговца

#### Другие сервисы
- TraderService - управление торговцами
- PlanetService - управление планетами
- ProjectService - управление проектами (использует триггеры БД)
- RouteService - управление маршрутами

### 4. REST API с Swagger документацией

Реализованы контроллеры:
- **TraderController** - `/api/traders`
- **PlanetController** - `/api/planets`
- **MessageController** - `/api/messages` (использует PL/pgSQL)
- **EventController** - `/api/events` (использует PL/pgSQL)
- **ProjectController** - `/api/projects`
- **EmpireController** - `/api/empire` (использует PL/pgSQL)
- **RouteController** - `/api/routes`

Все контроллеры документированы с помощью Swagger/OpenAPI аннотаций.

### 5. Конфигурация

- Настроен Swagger через `SwaggerConfig`
- Добавлен глобальный обработчик исключений `GlobalExceptionHandler`
- Обновлены настройки приложения

## Использование PL/pgSQL функций

Все функции из этапа 2 используются в сервисах:

1. **send_message()** → `MessageService.sendMessage()`
2. **resolve_crisis()** → `EventService.resolveCrisis()`
3. **get_empire_resources()** → `EmpireService.getEmpireResources()`

Функции вызываются через `EntityManager.createNativeQuery()`.

## Триггеры базы данных

Триггеры из этапа 2 автоматически работают:
- **rebellion_check** - обновляет статус мятежа при изменении лояльности
- **resource_check** - проверяет и списывает ресурсы при создании проекта

## Структура проекта

```
src/main/java/com/example/is_rogue_trader/
├── config/          # Конфигурация (Swagger)
├── controller/      # REST контроллеры (7 контроллеров)
├── dto/             # Data Transfer Objects (6 DTO)
├── exception/       # Обработчики исключений
├── model/
│   ├── entity/     # JPA сущности (11 сущностей)
│   └── enums/       # Перечисления (4 enum)
├── repository/      # JPA репозитории (10 репозиториев)
└── service/         # Бизнес-логика (7 сервисов)
```

## Документация

- `docs/class_diagram.md` - диаграмма классов
- `docs/API_DOCUMENTATION.md` - описание API
- `docs/SETUP.md` - инструкция по запуску

## Запуск

1. Настройте базу данных (выполните SQL скрипты из `stage2sql/`)
2. Настройте `application-dev.properties`
3. Запустите: `./gradlew bootRun`
4. Откройте Swagger UI: http://localhost:40000/swagger-ui.html

## Требования выполнены

✅ Диаграмма классов создана  
✅ Уровень хранения реализован на основе БД из этапа 2  
✅ Используются PL/pgSQL функции из этапа 2 (не заменены альтернативной реализацией)  
✅ Уровень бизнес-логики реализован на основе описания бизнес-процессов  
✅ Swagger документация добавлена ко всем эндпоинтам  

