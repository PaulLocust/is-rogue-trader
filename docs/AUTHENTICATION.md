# Документация по авторизации и JWT токенам

## Обзор

Система использует JWT (JSON Web Tokens) для аутентификации и авторизации пользователей. Все защищенные эндпоинты требуют наличия валидного JWT токена в заголовке запроса.

## Регистрация

### POST /api/auth/register

Регистрирует нового пользователя и возвращает JWT токен.

**Запрос:**
```json
{
  "email": "newtrader@dynasty.ru",
  "password": "password123",
  "role": "TRADER",
  "dynastyName": "Valancius",
  "warrantNumber": "WARRANT-002"
}
```

**Ответ:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "newtrader@dynasty.ru",
  "role": "TRADER"
}
```

**Пример с curl:**
```bash
curl -X POST http://localhost:40000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newtrader@dynasty.ru",
    "password": "password123",
    "role": "TRADER",
    "dynastyName": "Valancius",
    "warrantNumber": "WARRANT-002"
  }'
```

## Авторизация

### POST /api/auth/login

Авторизует существующего пользователя и возвращает JWT токен.

**Запрос:**
```json
{
  "email": "trader@dynasty.ru",
  "password": "password123"
}
```

**Ответ:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "trader@dynasty.ru",
  "role": "TRADER"
}
```

**Пример с curl:**
```bash
curl -X POST http://localhost:40000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "trader@dynasty.ru",
    "password": "password123"
  }'
```

## Использование JWT токена

После получения токена, добавьте его в заголовок `Authorization` всех защищенных запросов:

```
Authorization: Bearer <ваш_токен>
```

**Пример запроса с токеном:**
```bash
curl -X GET http://localhost:40000/api/traders/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Защищенные эндпоинты

Все эндпоинты, кроме `/api/auth/**`, требуют авторизации:

- `/api/traders/**` - управление торговцами
- `/api/planets/**` - управление планетами
- `/api/messages/**` - управление сообщениями
- `/api/events/**` - управление событиями
- `/api/projects/**` - управление проектами
- `/api/empire/**` - информация об империи
- `/api/routes/**` - управление маршрутами

## Публичные эндпоинты

Следующие эндпоинты доступны без авторизации:

- `/api/auth/register` - регистрация
- `/api/auth/login` - авторизация
- `/swagger-ui/**` - Swagger UI
- `/api-docs/**` - OpenAPI документация

## Роли пользователей

Система поддерживает следующие роли:

- `TRADER` - вольный торговец
- `GOVERNOR` - губернатор планеты
- `ASTROPATH` - астропат
- `NAVIGATOR` - навигатор

## Настройка JWT

В файле `application.properties` можно настроить:

```properties
# Секретный ключ для подписи JWT токенов (минимум 256 бит)
jwt.secret=your-secret-key-here

# Время жизни токена в миллисекундах (по умолчанию 24 часа)
jwt.expiration=86400000
```

**Важно:** В продакшене обязательно измените `jwt.secret` на безопасный случайный ключ!

## Swagger UI

В Swagger UI можно авторизоваться:

1. Откройте http://localhost:40000/swagger-ui.html
2. Нажмите кнопку "Authorize" в правом верхнем углу
3. Введите токен в формате: `Bearer <ваш_токен>`
4. Нажмите "Authorize"

После этого все запросы в Swagger UI будут автоматически включать токен в заголовки.

## Безопасность

- Пароли хранятся в БД в виде BCrypt хешей
- JWT токены подписываются с использованием HMAC SHA-256
- Токены содержат информацию о пользователе (email, userId, role)
- Токены имеют срок действия (по умолчанию 24 часа)
- Все защищенные эндпоинты проверяют валидность токена

## Обработка ошибок

### Неверные учетные данные
```json
{
  "error": "Неверный email или пароль",
  "status": "error"
}
```

### Отсутствует токен
```json
{
  "error": "Unauthorized",
  "status": "error"
}
```

### Истекший токен
```json
{
  "error": "JWT token has expired",
  "status": "error"
}
```

