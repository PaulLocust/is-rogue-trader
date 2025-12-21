# Авторизация и JWT - Реализация

## Что было добавлено

### 1. Зависимости
- Spring Security (`spring-boot-starter-security`)
- JWT библиотека (`jjwt` версии 0.12.3)

### 2. Компоненты безопасности

#### JwtTokenProvider
- Генерация JWT токенов
- Валидация токенов
- Извлечение данных из токенов (email, userId, role)

#### JwtAuthenticationFilter
- Фильтр для проверки JWT токенов в запросах
- Автоматическая установка аутентификации в SecurityContext

#### SecurityConfig
- Настройка Spring Security
- Конфигурация CORS
- Настройка публичных и защищенных эндпоинтов
- Интеграция JWT фильтра

### 3. Сервисы и контроллеры

#### AuthService
- Регистрация новых пользователей
- Авторизация существующих пользователей
- Хеширование паролей с помощью BCrypt
- Создание профилей в зависимости от роли

#### AuthController
- `POST /api/auth/register` - регистрация
- `POST /api/auth/login` - авторизация

### 4. DTO

- `LoginRequest` - запрос на авторизацию
- `RegisterRequest` - запрос на регистрацию
- `AuthResponse` - ответ с JWT токеном

### 5. Swagger интеграция

- Добавлена поддержка JWT в Swagger UI
- Все защищенные эндпоинты помечены `@SecurityRequirement`
- Кнопка "Authorize" в Swagger UI для ввода токена

## Использование

### Регистрация
```bash
curl -X POST http://localhost:40000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newtrader@dynasty.ru",
    "password": "password123",
    "role": "TRADER",
    "dynastyName": "Valancius"
  }'
```

### Авторизация
```bash
curl -X POST http://localhost:40000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "trader@dynasty.ru",
    "password": "password123"
  }'
```

### Использование токена
```bash
curl -X GET http://localhost:40000/api/traders/1 \
  -H "Authorization: Bearer <ваш_токен>"
```

## Настройки

В `application.properties`:
```properties
jwt.secret=your-secret-key-here
jwt.expiration=86400000  # 24 часа
```

## Публичные эндпоинты

- `/api/auth/**` - регистрация и авторизация
- `/swagger-ui/**` - Swagger UI
- `/api-docs/**` - OpenAPI документация

## Защищенные эндпоинты

Все остальные эндпоинты требуют JWT токен в заголовке `Authorization: Bearer <token>`

## Безопасность

- Пароли хешируются с помощью BCrypt
- JWT токены подписываются HMAC SHA-256
- Токены содержат userId, email и role
- Токены имеют срок действия

