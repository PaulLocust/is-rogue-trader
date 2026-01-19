-- ============================================
-- Database Schema Initialization Script
-- This script runs automatically on application startup
-- ============================================

-- 1. Users table (all roles)
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('TRADER', 'GOVERNOR', 'ASTROPATH', 'NAVIGATOR'))
);

-- 2. Rogue traders table
CREATE TABLE IF NOT EXISTS rogue_traders (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL REFERENCES users(id),
    dynasty_name VARCHAR(255) NOT NULL,
    warrant_number VARCHAR(100) UNIQUE,
    total_wealth DECIMAL(20,2) DEFAULT 1000000.00,
    influence INT DEFAULT 50 CHECK (influence >= 0 AND influence <= 100)
);

-- 3. Planets table
CREATE TABLE IF NOT EXISTS planets (
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

-- 4. Governors table
CREATE TABLE IF NOT EXISTS governors (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL REFERENCES users(id),
    planet_id INT UNIQUE NOT NULL REFERENCES planets(id)
);

-- 5. Astropaths table
CREATE TABLE IF NOT EXISTS astropaths (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL REFERENCES users(id),
    psi_level INT CHECK (psi_level BETWEEN 1 AND 10)
);

-- 6. Navigators table
CREATE TABLE IF NOT EXISTS navigators (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL REFERENCES users(id),
    house_name VARCHAR(100)
);

-- 7. Routes table
CREATE TABLE IF NOT EXISTS routes (
    id SERIAL PRIMARY KEY,
    from_planet_id INT NOT NULL REFERENCES planets(id),
    to_planet_id INT NOT NULL REFERENCES planets(id),
    navigator_id INT NOT NULL REFERENCES navigators(id),
    is_stable BOOLEAN DEFAULT TRUE,
    UNIQUE(from_planet_id, to_planet_id)
);

-- 8. Messages table
CREATE TABLE IF NOT EXISTS messages (
    id SERIAL PRIMARY KEY,
    sender_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    message_type VARCHAR(50),
    command_id BIGINT,
    resources_wealth DECIMAL(15,2) DEFAULT 0,
    resources_industry DECIMAL(15,2) DEFAULT 0,
    resources_resources DECIMAL(15,2) DEFAULT 0,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered BOOLEAN DEFAULT FALSE,
    distorted BOOLEAN DEFAULT FALSE,
    distortion_chance DECIMAL(3,2) DEFAULT 0.1,
    completed BOOLEAN DEFAULT FALSE,
    completion_date TIMESTAMP,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- 9. Upgrades table
CREATE TABLE IF NOT EXISTS upgrades (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    cost_wealth DECIMAL(15,2) NOT NULL,
    cost_industry DECIMAL(15,2) NOT NULL,
    cost_resources DECIMAL(15,2) NOT NULL,
    suitable_types VARCHAR(20) NOT NULL CHECK (suitable_types IN (
        'AGRI_WORLD', 
        'FORGE_WORLD', 
        'MINING_WORLD',
        'CIVILIZED_WORLD', 
        'DEATH_WORLD', 
        'HIVE_WORLD', 
        'FEUDAL_WORLD'
    ))
);

-- 10. Many-to-many association table for planets and upgrades
CREATE TABLE IF NOT EXISTS planet_upgrades (
    planet_id INT NOT NULL REFERENCES planets(id) ON DELETE CASCADE,
    upgrade_id INT NOT NULL REFERENCES upgrades(id) ON DELETE CASCADE,
    PRIMARY KEY (planet_id, upgrade_id)
);

-- 11. Projects table
CREATE TABLE IF NOT EXISTS projects (
    id SERIAL PRIMARY KEY,
    planet_id INT NOT NULL REFERENCES planets(id),
    upgrade_id INT NOT NULL REFERENCES upgrades(id),
    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PLANNED' CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

-- 12. Events table
CREATE TABLE IF NOT EXISTS events (
    id SERIAL PRIMARY KEY,
    planet_id INT NOT NULL REFERENCES planets(id),
    event_type VARCHAR(50) NOT NULL CHECK (
        event_type IN (
            'INSURRECTION',
            'NATURAL_DISASTER',
            'ECONOMIC_CRISIS',
            'EXTERNAL_THREAT'
        )
    ),
    severity INT CHECK (severity BETWEEN 1 AND 10),
    description TEXT,
    resolved BOOLEAN DEFAULT FALSE,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
