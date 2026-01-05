-- ============================================
-- Database Triggers
-- This script runs automatically on application startup
-- ============================================

-- Trigger function for automatic rebellion status update
CREATE OR REPLACE FUNCTION update_rebellion_status()
RETURNS TRIGGER AS $func$
BEGIN
    IF NEW.loyalty < 30.0 AND OLD.loyalty >= 30.0 THEN
        NEW.is_rebellious := TRUE;
    ELSIF NEW.loyalty >= 30.0 AND OLD.loyalty < 30.0 THEN
        NEW.is_rebellious := FALSE;
    END IF;
    RETURN NEW;
END;
$func$ LANGUAGE plpgsql;

-- Drop trigger if exists and create new one
DROP TRIGGER IF EXISTS rebellion_check ON planets;
CREATE TRIGGER rebellion_check
BEFORE UPDATE ON planets
FOR EACH ROW
EXECUTE FUNCTION update_rebellion_status();

-- Trigger function for checking resources when creating a project
CREATE OR REPLACE FUNCTION check_project_resources()
RETURNS TRIGGER AS $func$
DECLARE
    planet_wealth DECIMAL;
    planet_industry DECIMAL;
    planet_resources DECIMAL;
    upgrade_cost_wealth DECIMAL;
    upgrade_cost_industry DECIMAL;
    upgrade_cost_resources DECIMAL;
BEGIN
    -- Get planet resources
    SELECT wealth, industry, resources 
    INTO planet_wealth, planet_industry, planet_resources
    FROM planets WHERE id = NEW.planet_id;
    
    -- Get upgrade cost
    SELECT cost_wealth, cost_industry, cost_resources
    INTO upgrade_cost_wealth, upgrade_cost_industry, upgrade_cost_resources
    FROM upgrades WHERE id = NEW.upgrade_id;
    
    -- Check resources
    IF planet_wealth < upgrade_cost_wealth OR 
       planet_industry < upgrade_cost_industry OR 
       planet_resources < upgrade_cost_resources THEN
        RAISE EXCEPTION 'Insufficient resources on planet for this upgrade';
    END IF;
    
    -- Deduct resources
    UPDATE planets 
    SET wealth = wealth - upgrade_cost_wealth,
        industry = industry - upgrade_cost_industry,
        resources = resources - upgrade_cost_resources
    WHERE id = NEW.planet_id;
    
    RETURN NEW;
END;
$func$ LANGUAGE plpgsql;

-- Drop trigger if exists and create new one
DROP TRIGGER IF EXISTS resource_check ON projects;
CREATE TRIGGER resource_check
BEFORE INSERT ON projects
FOR EACH ROW
WHEN (NEW.status = 'PLANNED')
EXECUTE FUNCTION check_project_resources();

-- Trigger function for checking upgrade compatibility with planet type
CREATE OR REPLACE FUNCTION check_upgrade_compatibility()
RETURNS TRIGGER AS $func$
DECLARE
    planet_type_var VARCHAR(20);
    upgrade_type_var VARCHAR(20);
BEGIN
    -- Get planet type
    SELECT planet_type INTO planet_type_var
    FROM planets WHERE id = NEW.planet_id;
    
    -- Get upgrade suitable types
    SELECT suitable_types INTO upgrade_type_var
    FROM upgrades WHERE id = NEW.upgrade_id;
    
    -- Check compatibility
    IF planet_type_var != upgrade_type_var THEN
        RAISE EXCEPTION 'Upgrade type % is not compatible with planet type %', 
            upgrade_type_var, planet_type_var;
    END IF;
    
    RETURN NEW;
END;
$func$ LANGUAGE plpgsql;

-- Drop trigger if exists and create new one
DROP TRIGGER IF EXISTS compatibility_check ON projects;
CREATE TRIGGER compatibility_check
BEFORE INSERT ON projects
FOR EACH ROW
EXECUTE FUNCTION check_upgrade_compatibility();

-- Trigger to automatically add to planet_upgrades when project is completed
CREATE OR REPLACE FUNCTION add_to_planet_upgrades()
RETURNS TRIGGER AS $func$
BEGIN
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        -- Add to the many-to-many association table
        INSERT INTO planet_upgrades (planet_id, upgrade_id)
        VALUES (NEW.planet_id, NEW.upgrade_id)
        ON CONFLICT (planet_id, upgrade_id) DO NOTHING;
    END IF;
    RETURN NEW;
END;
$func$ LANGUAGE plpgsql;

-- Drop trigger if exists and create new one
DROP TRIGGER IF EXISTS project_completion ON projects;
CREATE TRIGGER project_completion
AFTER UPDATE ON projects
FOR EACH ROW
EXECUTE FUNCTION add_to_planet_upgrades();
