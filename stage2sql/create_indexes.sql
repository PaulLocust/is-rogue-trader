-- Создание индексов для оптимизации

-- Для авторизации
CREATE INDEX idx_users_email ON users(email);

-- Для карты империи
CREATE INDEX idx_planets_trader_rebellious ON planets(trader_id, is_rebellious);
CREATE INDEX idx_planets_type ON planets(planet_type);

-- Для сообщений
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_receiver ON messages(receiver_id);
CREATE INDEX idx_messages_sent_at ON messages(sent_at DESC);

-- Для событий
CREATE INDEX idx_events_planet_resolved ON events(planet_id, resolved);

-- Для проектов
CREATE INDEX idx_projects_planet_status ON projects(planet_id, status);
CREATE INDEX idx_projects_upgrade ON projects(upgrade_id);

-- Для маршрутов
CREATE INDEX idx_routes_navigator ON routes(navigator_id);
CREATE INDEX idx_routes_from_to ON routes(from_planet_id, to_planet_id);

-- Для губернаторов
CREATE INDEX idx_governors_planet ON governors(planet_id);

-- Для улучшений (оптимизация проверки совместимости)
CREATE INDEX idx_upgrades_type ON upgrades(suitable_types);

-- Для ассоциативной таблицы planet_upgrades (быстрый поиск установленных улучшений)
CREATE INDEX idx_planet_upgrades_planet ON planet_upgrades(planet_id);
CREATE INDEX idx_planet_upgrades_upgrade ON planet_upgrades(upgrade_id);

-- Для навигаторов (поиск по дому)
CREATE INDEX idx_navigators_house ON navigators(house_name);

-- Для вольных торговцев (поиск по номеру патента)
CREATE INDEX idx_rogue_traders_warrant ON rogue_traders(warrant_number);

-- Обоснование индексов для отчета:
-- 1. idx_planet_upgrades_planet и idx_planet_upgrades_upgrade - критически важны для 
--    прецедента "Улучшение инфраструктуры планеты" (ID:2). Позволяют быстро находить
--    какие улучшения установлены на планете и на каких планетах установлено улучшение.
--    Время поиска: O(log n) вместо O(n).
--    
-- 2. idx_upgrades_type - важен для триггера check_upgrade_compatibility() в прецеденте 
--    "Улучшение инфраструктуры планеты". Ускоряет проверку совместимости типа улучшения
--    с типом планеты при создании проекта.
--    
-- 3. idx_planets_trader_rebellious - важен для прецедента "Возврат мятежной планеты" (ID:3).
--    Позволяет быстро находить все мятежные планеты конкретного торговца для отображения
--    на карте империи.
--    
-- 4. idx_messages_sender и idx_messages_receiver - важны для прецедента 
--    "Обработка кризисного события" (ID:1). Ускоряют поиск сообщений между участниками
--    кризиса для принятия решений.
--    
-- 5. idx_routes_from_to - важен для прецедента "Создание нового маршрута" (ID:6).
--    Позволяет быстро проверять существование маршрута между планетами при создании
--    нового маршрута Навигатором.
--    
-- 6. idx_events_planet_resolved - важен для дашборда Вольного Торговца (FR2).
--    Позволяет быстро находить активные события на планетах для отображения
--    на сводном дашборде империи.