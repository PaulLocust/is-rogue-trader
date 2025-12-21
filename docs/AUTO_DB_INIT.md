# Автоматическая инициализация базы данных

## Обзор

База данных автоматически инициализируется при запуске Spring Boot приложения. Все таблицы, функции, триггеры и индексы создаются автоматически.

## Как это работает

Spring Boot автоматически выполняет SQL скрипты при запуске приложения, если настроено:

```properties
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:sql/schema.sql
spring.sql.init.data-locations=classpath:sql/data.sql
```

## Структура файлов

### `src/main/resources/sql/schema.sql`
Содержит:
1. **Таблицы** - все 11 таблиц с `CREATE TABLE IF NOT EXISTS`
2. **Функции** - все PL/pgSQL функции (`send_message`, `resolve_crisis`, `get_empire_resources`)
3. **Триггеры** - триггеры для автоматического обновления статуса мятежа и проверки ресурсов
4. **Индексы** - все индексы для оптимизации запросов

### `src/main/resources/sql/data.sql`
Содержит тестовые данные:
- Пользователи (с хешированными паролями BCrypt)
- Торговцы
- Планеты
- Губернаторы, астропаты, навигаторы
- Маршруты
- Улучшения
- События

## Настройки

В `application.properties`:

```properties
# SQL initialization
spring.sql.init.mode=always                    # Всегда выполнять скрипты
spring.sql.init.schema-locations=classpath:sql/schema.sql  # Скрипт схемы
spring.sql.init.data-locations=classpath:sql/data.sql       # Скрипт данных
spring.sql.init.continue-on-error=true        # Продолжать при ошибках
```

## Режимы работы

### `spring.sql.init.mode=always`
- Скрипты выполняются при каждом запуске
- Подходит для разработки

### `spring.sql.init.mode=embedded`
- Скрипты выполняются только для встроенных БД (H2, HSQL)
- Не подходит для PostgreSQL

### `spring.sql.init.mode=never`
- Скрипты не выполняются
- Используйте для продакшена

## Безопасность

- Используется `CREATE TABLE IF NOT EXISTS` - таблицы не пересоздаются
- Используется `CREATE OR REPLACE FUNCTION` - функции обновляются
- Используется `DROP TRIGGER IF EXISTS` - триггеры пересоздаются безопасно
- Используется `ON CONFLICT DO NOTHING` - данные не дублируются

## Тестовые данные

По умолчанию создаются:
- 5 пользователей (1 торговец, 2 губернатора, 1 астропат, 1 навигатор)
- 1 торговец (династия Valancius)
- 5 планет
- 2 губернатора
- 1 астропат (psi_level = 7)
- 1 навигатор (House of Ravens)
- 2 маршрута
- 5 улучшений
- 1 событие (восстание на Mining-Tertius)

**Пароль по умолчанию для всех тестовых пользователей:** `password123`

## Отключение автоматической инициализации

Для продакшена отключите автоматическую инициализацию:

```properties
spring.sql.init.mode=never
```

Или используйте профиль:

```properties
# application-prod.properties
spring.sql.init.mode=never
```

## Ручная инициализация

Если нужно выполнить скрипты вручную, используйте файлы из `stage2sql/`:

```bash
psql -U postgres -d is_rogue_trader -f stage2sql/run_all.sql
```

## Логирование

При запуске приложения вы увидите в логах:
```
Executing SQL script from URL [file:.../sql/schema.sql]
Executing SQL script from URL [file:.../sql/data.sql]
```

Если есть ошибки, они будут отображены в логах.

