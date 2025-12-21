-- ============================================
-- PL/pgSQL Functions
-- This script runs automatically on application startup
-- ============================================

-- Function for sending messages
CREATE OR REPLACE FUNCTION send_message(
    sender_id INT,
    receiver_id INT,
    message_content TEXT,
    distortion_chance DECIMAL DEFAULT 0.1
) RETURNS INT AS $func$
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
$func$ LANGUAGE plpgsql;

-- Function for resolving crisis
CREATE OR REPLACE FUNCTION resolve_crisis(
    event_id INT,
    action VARCHAR(10),
    resources_wealth DECIMAL DEFAULT 0,
    resources_industry DECIMAL DEFAULT 0
) RETURNS VOID AS $func$
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
$func$ LANGUAGE plpgsql;

-- Function for calculating empire resources
CREATE OR REPLACE FUNCTION get_empire_resources(trader_id_param INT)
RETURNS TABLE(
    total_wealth DECIMAL,
    total_industry DECIMAL,
    total_resources DECIMAL,
    planet_count BIGINT
) AS $func$
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
$func$ LANGUAGE plpgsql;

