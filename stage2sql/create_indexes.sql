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