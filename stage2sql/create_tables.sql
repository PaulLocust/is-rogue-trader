-- 1. Таблица пользователей (все роли)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('TRADER', 'GOVERNOR', 'ASTROPATH', 'NAVIGATOR'))
);

-- 2. Таблица вольных торговцев
CREATE TABLE rogue_traders (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL REFERENCES users(id),
    dynasty_name VARCHAR(255) NOT NULL,
    warrant_number VARCHAR(100) UNIQUE,
    total_wealth DECIMAL(20,2) DEFAULT 1000000.00,
    influence INT DEFAULT 50 CHECK (influence >= 0 AND influence <= 100)
);



-- 3. Таблица планет
CREATE TABLE planets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    planet_type VARCHAR(20) NOT NULL CHECK (planet_type IN (
        'AGRI_WORLD', 
        'FORGE_WORLD', 
        'MINING_WORLD',
        'CIVILIZED_WORLD', 
        'DEATH_WORLD', 
        'HIVE_WORLD', 
        'FEUDAL_WORLD'
    )),
    loyalty DECIMAL(5,2) DEFAULT 50.0 CHECK (loyalty >= 0 AND loyalty <= 100),
    wealth DECIMAL(15,2) DEFAULT 0,
    industry DECIMAL(15,2) DEFAULT 0,
    resources DECIMAL(15,2) DEFAULT 0,
    is_rebellious BOOLEAN DEFAULT FALSE,
    trader_id INT NOT NULL REFERENCES rogue_traders(id)
);

-- 4. Таблица губернаторов
CREATE TABLE governors (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL REFERENCES users(id),
    planet_id INT UNIQUE NOT NULL REFERENCES planets(id)
);

-- 5. Таблица астропатов
CREATE TABLE astropaths (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL REFERENCES users(id),
    psi_level INT CHECK (psi_level BETWEEN 1 AND 10)
);

-- 6. Таблица навигаторов
CREATE TABLE navigators (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL REFERENCES users(id),
    house_name VARCHAR(100)
);

-- 7. Таблица маршрутов
CREATE TABLE routes (
    id SERIAL PRIMARY KEY,
    from_planet_id INT NOT NULL REFERENCES planets(id),
    to_planet_id INT NOT NULL REFERENCES planets(id),
    navigator_id INT NOT NULL REFERENCES navigators(id),
    is_stable BOOLEAN DEFAULT TRUE,
    UNIQUE(from_planet_id, to_planet_id)
);

-- 8. Таблица сообщений
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    sender_id INT NOT NULL REFERENCES users(id),
    receiver_id INT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered BOOLEAN DEFAULT FALSE,
    distorted BOOLEAN DEFAULT FALSE
);

-- 9. Таблица улучшений
CREATE TABLE upgrades (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cost_wealth DECIMAL(15,2) NOT NULL,
    cost_industry DECIMAL(15,2) NOT NULL,
    cost_resources DECIMAL(15,2) NOT NULL,
    suitable_types VARCHAR(20) NOT NULL CHECK (
        suitable_types IN (
            'AGRI_WORLD', 
            'FORGE_WORLD', 
            'MINING_WORLD',
            'CIVILIZED_WORLD', 
            'DEATH_WORLD', 
            'HIVE_WORLD', 
            'FEUDAL_WORLD'
        )
    )
);

-- 10. Таблица проектов
CREATE TABLE projects (
    id SERIAL PRIMARY KEY,
    planet_id INT NOT NULL REFERENCES planets(id),
    upgrade_id INT NOT NULL REFERENCES upgrades(id),
    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PLANNED',
    UNIQUE(planet_id, upgrade_id, status)
);

-- 11. Таблица событий
CREATE TABLE events (
    id SERIAL PRIMARY KEY,
    planet_id INT NOT NULL REFERENCES planets(id),
    event_type VARCHAR(50) NOT NULL CHECK (
        event_type IN (
            'INSURRECTION',
            'NATURAL_DISASTEL', 
            'ECONOMIC_CRISIS',
            'EXTERNAL_THREAT'
        )
    ),
    severity INT CHECK (severity BETWEEN 1 AND 10),
    description TEXT,
    resolved BOOLEAN DEFAULT FALSE,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);