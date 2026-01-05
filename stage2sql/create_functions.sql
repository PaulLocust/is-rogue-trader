-- Function for sending messages
CREATE OR REPLACE FUNCTION send_message(
    sender_id INT,
    receiver_id INT,
    message_content TEXT,
    distortion_chance DECIMAL DEFAULT 0.1
) RETURNS INT AS $$
DECLARE
    message_id INT;
    astropath_psi INT;
    distorted BOOLEAN;
    distorted_content TEXT;
BEGIN
    -- Check if sender is an astropath
    SELECT psi_level INTO astropath_psi 
    FROM astropaths WHERE user_id = sender_id;
    
    -- Adjust distortion chance
    distorted := (RANDOM() < distortion_chance);
    IF astropath_psi IS NOT NULL AND astropath_psi < 5 THEN
        distorted := (RANDOM() < (distortion_chance + 0.2));
    END IF;
    
    -- Apply distortion
    IF distorted THEN
        distorted_content := message_content || ' [DISTORTED IN WARP]';
    ELSE
        distorted_content := message_content;
    END IF;
    
    -- Save message
    INSERT INTO messages (sender_id, receiver_id, content, distorted)
    VALUES (sender_id, receiver_id, distorted_content, distorted)
    RETURNING id INTO message_id;
    
    RETURN message_id;
END;
$$ LANGUAGE plpgsql;

-- Function for resolving crisis
CREATE OR REPLACE FUNCTION resolve_crisis(
    event_id INT,
    action VARCHAR(10),
    resources_wealth DECIMAL DEFAULT 0,
    resources_industry DECIMAL DEFAULT 0
) RETURNS VOID AS $$
DECLARE
    target_planet_id INT;
    event_severity INT;
BEGIN
    -- Get event information
    SELECT planet_id, severity INTO target_planet_id, event_severity
    FROM events WHERE id = event_id;
    
    IF action = 'HELP' THEN
        -- Allocate resources
        UPDATE planets 
        SET wealth = wealth + resources_wealth,
            industry = industry + resources_industry,
            loyalty = LEAST(100, loyalty + 15)
        WHERE id = target_planet_id;
        
        UPDATE events SET resolved = TRUE WHERE id = event_id;
        
    ELSIF action = 'IGNORE' THEN
        -- Ignore the problem
        UPDATE planets 
        SET loyalty = GREATEST(0, loyalty - 20),
            wealth = wealth * 0.9,
            industry = industry * 0.85
        WHERE id = target_planet_id;
        
        UPDATE events SET resolved = TRUE WHERE id = event_id;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function for calculating empire resources
CREATE OR REPLACE FUNCTION get_empire_resources(trader_id_param INT)
RETURNS TABLE(
    total_wealth DECIMAL,
    total_industry DECIMAL,
    total_resources DECIMAL,
    planet_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COALESCE(SUM(wealth), 0),
        COALESCE(SUM(industry), 0),
        COALESCE(SUM(resources), 0),
        COUNT(*)
    FROM planets
    WHERE trader_id = trader_id_param AND is_rebellious = FALSE;
END;
$$ LANGUAGE plpgsql;

-- Function to get installed upgrades for a planet
CREATE OR REPLACE FUNCTION get_installed_upgrades(planet_id_param INT)
RETURNS TABLE(
    upgrade_id INT,
    upgrade_name VARCHAR(100),
    upgrade_description TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.id,
        u.name,
        u.description
    FROM upgrades u
    INNER JOIN planet_upgrades pu ON u.id = pu.upgrade_id
    WHERE pu.planet_id = planet_id_param
    ORDER BY u.name;
END;
$$ LANGUAGE plpgsql;

-- Function to check if upgrade can be installed on planet
CREATE OR REPLACE FUNCTION can_install_upgrade(
    planet_id_param INT,
    upgrade_id_param INT
) RETURNS BOOLEAN AS $$
DECLARE
    planet_type_var VARCHAR(20);
    upgrade_type_var VARCHAR(20);
BEGIN
    -- Get planet type
    SELECT planet_type INTO planet_type_var
    FROM planets WHERE id = planet_id_param;
    
    -- Get upgrade type
    SELECT suitable_types INTO upgrade_type_var
    FROM upgrades WHERE id = upgrade_id_param;
    
    -- Check compatibility
    RETURN planet_type_var = upgrade_type_var;
END;
$$ LANGUAGE plpgsql;

-- Function to get planet statistics with installed upgrades
CREATE OR REPLACE FUNCTION get_planet_stats_with_upgrades(planet_id_param INT)
RETURNS TABLE(
    planet_name VARCHAR(100),
    planet_type VARCHAR(20),
    loyalty DECIMAL(5,2),
    wealth DECIMAL(15,2),
    industry DECIMAL(15,2),
    resources DECIMAL(15,2),
    installed_upgrades_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.name,
        p.planet_type,
        p.loyalty,
        p.wealth,
        p.industry,
        p.resources,
        (SELECT COUNT(*) FROM planet_upgrades pu 
         WHERE pu.planet_id = p.id) as installed_count
    FROM planets p
    WHERE p.id = planet_id_param;
END;
$$ LANGUAGE plpgsql;