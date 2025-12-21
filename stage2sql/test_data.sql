-- Insert test data in correct order

-- 1. Users first
INSERT INTO users (id, email, password_hash, role) VALUES
(1, 'trader@dynasty.ru', 'hash123', 'TRADER'),
(2, 'governor1@world.ru', 'hash456', 'GOVERNOR'),
(3, 'governor2@world.ru', 'hash789', 'GOVERNOR'),
(4, 'astropath@astra.ru', 'hash101', 'ASTROPATH'),
(5, 'navigator@house.ru', 'hash112', 'NAVIGATOR');

-- 1.1. Rogue trader
INSERT INTO rogue_traders (user_id, dynasty_name, warrant_number) VALUES
(1, 'Valancius', 'WARRANT-001');

-- 2. Planets (using only allowed types from CHECK constraint)
INSERT INTO planets (id, name, planet_type, loyalty, wealth, industry, resources, trader_id, is_rebellious) VALUES
(1, 'Agri-Prima', 'AGRI_WORLD', 85.0, 10000, 2000, 5000, 1, false),
(2, 'Forge-Secundus', 'FORGE_WORLD', 65.0, 5000, 15000, 3000, 1, false),
(3, 'Mining-Tertius', 'MINING_WORLD', 25.0, 3000, 2000, 25000, 1, true),
(4, 'Necromunda', 'HIVE_WORLD', 70.0, 8000, 6000, 4000, 1, false),
(5, 'Catachan', 'DEATH_WORLD', 40.0, 1000, 500, 10000, 1, false);

-- 3. Governors
INSERT INTO governors (user_id, planet_id) VALUES
(2, 1),
(3, 2);

-- 4. Specialists
INSERT INTO astropaths (user_id, psi_level) VALUES (4, 7);
INSERT INTO navigators (user_id, house_name) VALUES (5, 'House of Ravens');

-- 5. Routes
INSERT INTO routes (from_planet_id, to_planet_id, navigator_id, is_stable) VALUES
(1, 2, 1, true),
(2, 3, 1, false);

-- 6. Upgrades (using only allowed types from CHECK constraint)
INSERT INTO upgrades (name, description, cost_wealth, cost_industry, cost_resources, suitable_types) VALUES
('Irrigation Systems', 'Increases food production', 5000, 2000, 1000, 'AGRI_WORLD'),
('Deep Mining Shafts', 'Increases resource extraction', 8000, 3000, 5000, 'MINING_WORLD'),
('Ark-Tech Factories', 'Increases industrial power', 15000, 10000, 3000, 'FORGE_WORLD'),
('Hive Architecture', 'Improves living conditions', 12000, 8000, 4000, 'HIVE_WORLD'),
('Shield Generators', 'Protection from harsh conditions', 7000, 4000, 6000, 'DEATH_WORLD');

-- 7. Events
INSERT INTO events (planet_id, event_type, severity, description, resolved) VALUES
(3, 'INSURRECTION', 7, 'Miners demand higher wages', false);

-- 8. Example correct project (will test all triggers)
INSERT INTO projects (planet_id, upgrade_id, status) VALUES
(1, 1, 'PLANNED');

-- Output information
SELECT 'Database created successfully!' as message;
SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'public';
SELECT COUNT(*) as index_count FROM pg_indexes WHERE schemaname = 'public';

-- Test triggers
SELECT 'Testing rebellion trigger:' as test;
UPDATE planets SET loyalty = 25.0 WHERE id = 2;
SELECT name, loyalty, is_rebellious FROM planets WHERE id = 2;

SELECT 'Testing compatibility trigger (should cause error):' as test;
-- This INSERT should cause an error due to type incompatibility
-- INSERT INTO projects (planet_id, upgrade_id, status) VALUES (1, 2, 'PLANNED');  -- AGRI_WORLD + MINING_WORLD

SELECT 'Test data insertion completed!' as message;
