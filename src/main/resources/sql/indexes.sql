-- ============================================
-- Database Indexes
-- This script runs automatically on application startup
-- ============================================

-- For authorization
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- For empire map
CREATE INDEX IF NOT EXISTS idx_planets_trader_rebellious ON planets(trader_id, is_rebellious);
CREATE INDEX IF NOT EXISTS idx_planets_type ON planets(planet_type);

-- For messages
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_receiver ON messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_messages_sent_at ON messages(sent_at DESC);

-- For events
CREATE INDEX IF NOT EXISTS idx_events_planet_resolved ON events(planet_id, resolved);

-- For projects
CREATE INDEX IF NOT EXISTS idx_projects_planet_status ON projects(planet_id, status);
CREATE INDEX IF NOT EXISTS idx_projects_upgrade ON projects(upgrade_id);

-- For routes
CREATE INDEX IF NOT EXISTS idx_routes_navigator ON routes(navigator_id);
CREATE INDEX IF NOT EXISTS idx_routes_from_to ON routes(from_planet_id, to_planet_id);

-- For governors
CREATE INDEX IF NOT EXISTS idx_governors_planet ON governors(planet_id);

-- For upgrades (optimization for compatibility check)
CREATE INDEX IF NOT EXISTS idx_upgrades_type ON upgrades(suitable_types);

