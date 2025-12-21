# Инструкция по запуску проекта

## Требования

- Java 17+
- PostgreSQL
- Gradle

## Настройка базы данных

1. Создайте базу данных PostgreSQL
2. Выполните SQL скрипты из директории `stage2sql/` в следующем порядке:
   - `create_tables.sql` - создание таблиц
   - `create_functions.sql` - создание PL/pgSQL функций
   - `create_triggers.sql` - создание триггеров
   - `create_indexes.sql` - создание индексов
   - `test_data.sql` - заполнение тестовыми данными (опционально)

Или используйте `run_all.sql` для выполнения всех скриптов сразу.

## Настройка приложения

Настройте переменные окружения или файлы конфигурации:

### application-dev.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Запуск приложения

```bash
./gradlew bootRun
```

Или через IDE запустите класс `IsRogueTraderApplication`.

## Доступ к API

После запуска приложение будет доступно по адресу: `http://localhost:40000`

### Swagger UI
- URL: http://localhost:40000/swagger-ui.html
- OpenAPI JSON: http://localhost:40000/api-docs

## Тестирование API

### Пример: Отправка сообщения
```bash
curl -X POST http://localhost:40000/api/messages \
  -H "Content-Type: application/json" \
  -d '{
    "senderId": 2,
    "receiverId": 1,
    "content": "Всё спокойно на Аграрий-Прима",
    "distortionChance": 0.1
  }'
```

### Пример: Получение ресурсов империи
```bash
curl http://localhost:40000/api/empire/1/resources
```

### Пример: Разрешение кризиса
```bash
curl -X POST http://localhost:40000/api/events/1/resolve \
  -H "Content-Type: application/json" \
  -d '{
    "action": "HELP",
    "wealth": 2000,
    "industry": 1000
  }'
```

## Структура проекта

```
src/main/java/com/example/is_rogue_trader/
├── config/          # Конфигурация (Swagger)
├── controller/      # REST контроллеры
├── dto/             # Data Transfer Objects
├── exception/       # Обработчики исключений
├── model/
│   ├── entity/      # JPA сущности
│   └── enums/       # Перечисления
├── repository/      # JPA репозитории
└── service/         # Бизнес-логика (использует PL/pgSQL функции)
```

## Использование PL/pgSQL функций

Система использует следующие функции из базы данных:

1. **send_message()** - в `MessageService.sendMessage()`
2. **resolve_crisis()** - в `EventService.resolveCrisis()`
3. **get_empire_resources()** - в `EmpireService.getEmpireResources()`

Все функции вызываются через `EntityManager.createNativeQuery()`.

