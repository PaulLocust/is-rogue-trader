-- Тестирование функций базы данных

\echo '=== ТЕСТИРОВАНИЕ ФУНКЦИЙ БАЗЫ ДАННЫХ ==='

\echo ''
\echo '1. Тестирование функции send_message():'
\echo '======================================='
DO $$
DECLARE
    msg_id INT;
BEGIN
    -- Отправляем сообщение от губернатора к торговцу
    msg_id := send_message(2, 1, 'Всё спокойно на Аграрий-Прима, лояльность высокая');
    RAISE NOTICE 'Отправлено сообщение ID: %', msg_id;
    
    -- Отправляем сообщение от астропата (с искажением)
    msg_id := send_message(4, 1, 'Обнаружен варп-шторм на маршруте к Фордж-Секундус', 0.3);
    RAISE NOTICE 'Отправлено сообщение от астропата ID: %', msg_id;
    
    -- Проверяем сообщения
    RAISE NOTICE 'Всего сообщений в базе: %', (SELECT COUNT(*) FROM messages);
END $$;

\echo ''
\echo '2. Тестирование функции resolve_crisis():'
\echo '========================================='
DO $$
BEGIN
    -- Сначала посмотрим текущее состояние планеты с событием
    RAISE NOTICE 'До обработки кризиса:';
    RAISE NOTICE 'Лояльность: %, Богатство: %, Индустрия: %', 
        (SELECT loyalty FROM planets WHERE id = 3),
        (SELECT wealth FROM planets WHERE id = 3),
        (SELECT industry FROM planets WHERE id = 3);
    
    -- Обрабатываем кризис с помощью ресурсов
    PERFORM resolve_crisis(1, 'HELP', 2000, 1000);
    
    RAISE NOTICE 'После помощи кризису:';
    RAISE NOTICE 'Лояльность: %, Богатство: %, Индустрия: %', 
        (SELECT loyalty FROM planets WHERE id = 3),
        (SELECT wealth FROM planets WHERE id = 3),
        (SELECT industry FROM planets WHERE id = 3);
    RAISE NOTICE 'Событие разрешено: %', (SELECT resolved FROM events WHERE id = 1);
    
    -- Добавим новое событие для теста игнорирования
    INSERT INTO events (planet_id, event_type, severity, description, resolved) VALUES
    (4, 'INSURRECTION', 5, 'Dissatisfaction with living conditions', false);
    
    -- Игнорируем проблему
    PERFORM resolve_crisis(2, 'IGNORE');
    
    RAISE NOTICE 'После игнорирования кризиса:';
    RAISE NOTICE 'Лояльность планеты 4: %', (SELECT loyalty FROM planets WHERE id = 4);
END $$;

\echo ''
\echo '3. Тестирование функции get_empire_resources():'
\echo '=============================================='
DO $$
DECLARE
    total_wealth DECIMAL;
    total_industry DECIMAL;
    total_resources DECIMAL;
    planet_count BIGINT;
BEGIN
    -- Вызываем функцию для торговца с id=1
    SELECT * INTO total_wealth, total_industry, total_resources, planet_count 
    FROM get_empire_resources(1);
    
    RAISE NOTICE 'Ресурсы империи торговца 1:';
    RAISE NOTICE 'Общее богатство: %', total_wealth;
    RAISE NOTICE 'Общая индустрия: %', total_industry;
    RAISE NOTICE 'Общие ресурсы: %', total_resources;
    RAISE NOTICE 'Количество лояльных планет: %', planet_count;
    
    -- Проверим вручную для сравнения
    RAISE NOTICE 'Проверка вручную (сумма по небунтующим планетам):';
    RAISE NOTICE 'Сумма богатства: %', (SELECT SUM(wealth) FROM planets WHERE trader_id = 1 AND is_rebellious = FALSE);
END $$;

\echo ''
\echo '4. Тестирование НОВЫХ функций для работы с ассоциативной таблицей:'
\echo '=================================================================='
DO $$
DECLARE
    compatible BOOLEAN;
    upgrade_rec RECORD;
    stats_rec RECORD;
BEGIN
    -- Тестирование функции can_install_upgrade
    RAISE NOTICE 'Тест совместимости улучшений:';
    
    -- Совместимый случай: AGRI_WORLD + Irrigation Systems (upgrade_id=1)
    SELECT can_install_upgrade(1, 1) INTO compatible;
    RAISE NOTICE 'Планета 1 (AGRI_WORLD) + Улучшение 1 (Irrigation): %', 
        CASE WHEN compatible THEN 'Совместимо ✓' ELSE 'Несовместимо ✗' END;
    
    -- Несовместимый случай: AGRI_WORLD + Deep Mining Shafts (upgrade_id=2)
    SELECT can_install_upgrade(1, 2) INTO compatible;
    RAISE NOTICE 'Планета 1 (AGRI_WORLD) + Улучшение 2 (Mining): %', 
        CASE WHEN compatible THEN 'Совместимо ✓' ELSE 'Несовместимо ✗' END;
    
    -- Тестирование функции get_installed_upgrades
    RAISE NOTICE '';
    RAISE NOTICE 'Установленные улучшения для планеты 1:';
    FOR upgrade_rec IN SELECT * FROM get_installed_upgrades(1) LOOP
        RAISE NOTICE '  - %: %', upgrade_rec.upgrade_name, upgrade_rec.upgrade_description;
    END LOOP;
    
    -- Тестирование функции get_planet_stats_with_upgrades
    RAISE NOTICE '';
    RAISE NOTICE 'Статистика планеты 1 с информацией об улучшениях:';
    FOR stats_rec IN SELECT * FROM get_planet_stats_with_upgrades(1) LOOP
        RAISE NOTICE 'Планета: %', stats_rec.planet_name;
        RAISE NOTICE 'Тип: %', stats_rec.planet_type;
        RAISE NOTICE 'Лояльность: %', stats_rec.loyalty;
        RAISE NOTICE 'Установлено улучшений: %', stats_rec.installed_upgrades_count;
    END LOOP;
END $$;

\echo ''
\echo '5. Тестирование триггеров:'
\echo '==========================='
DO $$
DECLARE
    is_compatible BOOLEAN;
BEGIN
    -- Тест триггера update_rebellion_status
    RAISE NOTICE 'Тест триггера мятежа:';
    RAISE NOTICE 'До: Планета 2 - лояльность: %, бунтующая: %', 
        (SELECT loyalty FROM planets WHERE id = 2),
        (SELECT is_rebellious FROM planets WHERE id = 2);
    
    UPDATE planets SET loyalty = 50.0 WHERE id = 2;
    
    RAISE NOTICE 'После (лояльность 50): Планета 2 - лояльность: %, бунтующая: %', 
        (SELECT loyalty FROM planets WHERE id = 2),
        (SELECT is_rebellious FROM planets WHERE id = 2);
    
    -- Вернем обратно
    UPDATE planets SET loyalty = 65.0 WHERE id = 2;
    
    -- Тест триггера check_project_resources
    RAISE NOTICE '';
    RAISE NOTICE 'Тест триггера проверки ресурсов:';
    
    -- Проверим текущие ресурсы планеты 1
    RAISE NOTICE 'Ресурсы планеты 1: богатство=%, индустрия=%, ресурсы=%',
        (SELECT wealth FROM planets WHERE id = 1),
        (SELECT industry FROM planets WHERE id = 1),
        (SELECT resources FROM planets WHERE id = 1);
    
    -- Стоимость улучшения 1
    RAISE NOTICE 'Стоимость улучшения 1: богатство=%, индустрия=%, ресурсы=%',
        (SELECT cost_wealth FROM upgrades WHERE id = 1),
        (SELECT cost_industry FROM upgrades WHERE id = 1),
        (SELECT cost_resources FROM upgrades WHERE id = 1);
    
    -- Попробуем добавить новый проект
    BEGIN
        -- Теперь создадим новый проект (совместимый и с достаточными ресурсами)
        INSERT INTO projects (planet_id, upgrade_id, status) VALUES (1, 1, 'PLANNED');
        RAISE NOTICE 'Проект успешно создан!';
        
        -- Проверим списание ресурсов
        RAISE NOTICE 'Ресурсы после списания: богатство=%, индустрия=%, ресурсы=%',
            (SELECT wealth FROM planets WHERE id = 1),
            (SELECT industry FROM planets WHERE id = 1),
            (SELECT resources FROM planets WHERE id = 1);
    EXCEPTION 
        WHEN OTHERS THEN
            RAISE NOTICE 'Ошибка при создании проекта: %', SQLERRM;
    END;
    
    -- Тест триггера check_upgrade_compatibility
    RAISE NOTICE '';
    RAISE NOTICE 'Тест триггера проверки совместимости:';
    RAISE NOTICE 'Тип планеты 1: %', (SELECT planet_type FROM planets WHERE id = 1);
    RAISE NOTICE 'Тип улучшения 2: %', (SELECT suitable_types FROM upgrades WHERE id = 2);
    
    BEGIN
        -- Попробуем создать несовместимый проект (должна быть ошибка)
        INSERT INTO projects (planet_id, upgrade_id, status) VALUES (1, 2, 'PLANNED');
        RAISE NOTICE 'ОШИБКА: Этот проект не должен был быть создан!';
    EXCEPTION 
        WHEN OTHERS THEN
            RAISE NOTICE 'Триггер проверки совместимости сработал правильно: %', SQLERRM;
    END;
    
    -- Тест триггера add_to_planet_upgrades
    RAISE NOTICE '';
    RAISE NOTICE 'Тест триггера добавления в ассоциативную таблицу:';
    RAISE NOTICE 'Количество записей в planet_upgrades до: %', (SELECT COUNT(*) FROM planet_upgrades);
    
    -- Создадим новый проект и завершим его
    INSERT INTO projects (planet_id, upgrade_id, status) VALUES (2, 3, 'PLANNED');
    UPDATE projects SET status = 'COMPLETED' WHERE planet_id = 2 AND upgrade_id = 3;
    
    RAISE NOTICE 'Количество записей в planet_upgrades после: %', (SELECT COUNT(*) FROM planet_upgrades);
    RAISE NOTICE 'Запись в planet_upgrades: планета=%, улучшение=%', 
        (SELECT planet_id FROM planet_upgrades WHERE planet_id = 2 AND upgrade_id = 3),
        (SELECT upgrade_id FROM planet_upgrades WHERE planet_id = 2 AND upgrade_id = 3);
END $$;

\echo ''
\echo '6. Тестирование ограничений (CHECK constraints):'
\echo '==============================================='
DO $$
BEGIN
    -- Попробуем вставить некорректный тип планеты
    BEGIN
        INSERT INTO planets (name, planet_type, trader_id) 
        VALUES ('Тестовая', 'INVALID_TYPE', 1);
        RAISE NOTICE 'ОШИБКА: Вставка должна была завершиться ошибкой!';
    EXCEPTION 
        WHEN OTHERS THEN
            RAISE NOTICE 'CHECK constraint сработал: %', SQLERRM;
    END;
    
    -- Попробуем вставить некорректный тип события
    BEGIN
        INSERT INTO events (planet_id, event_type, severity, description) 
        VALUES (1, 'INVALID_EVENT', 5, 'Test');
        RAISE NOTICE 'ОШИБКА: Вставка должна была завершиться ошибкой!';
    EXCEPTION 
        WHEN OTHERS THEN
            RAISE NOTICE 'CHECK constraint сработал: %', SQLERRM;
    END;
    
    -- Попробуем вставить некорректную роль пользователя
    BEGIN
        INSERT INTO users (email, password_hash, role) 
        VALUES ('test@test.ru', 'hash', 'INVALID_ROLE');
        RAISE NOTICE 'ОШИБКА: Вставка должна была завершиться ошибкой!';
    EXCEPTION 
        WHEN OTHERS THEN
            RAISE NOTICE 'CHECK constraint сработал: %', SQLERRM;
    END;
    
    -- Попробуем вставить лояльность больше 100
    BEGIN
        INSERT INTO planets (name, planet_type, loyalty, trader_id) 
        VALUES ('Тестовая', 'AGRI_WORLD', 150.0, 1);
        RAISE NOTICE 'ОШИБКА: Вставка должна была завершиться ошибкой!';
    EXCEPTION 
        WHEN OTHERS THEN
            RAISE NOTICE 'CHECK constraint сработал: %', SQLERRM;
    END;
END $$;

\echo ''
\echo '7. Демонстрация работы ассоциативной таблицы M:N:'
\echo '================================================='
DO $$
DECLARE
    rec RECORD;
BEGIN
    RAISE NOTICE 'Демонстрация отношения многие-ко-многим через таблицу planet_upgrades:';
    RAISE NOTICE '---------------------------------------------------------------------';
    
    RAISE NOTICE 'Пример 1: Какие улучшения установлены на каждой планете?';
    FOR rec IN (
        SELECT 
            p.name as planet_name,
            STRING_AGG(u.name, ', ') as installed_upgrades
        FROM planets p
        LEFT JOIN planet_upgrades pu ON p.id = pu.planet_id
        LEFT JOIN upgrades u ON pu.upgrade_id = u.id
        GROUP BY p.id, p.name
        ORDER BY p.name
    ) LOOP
        RAISE NOTICE 'Планета "%": %', 
            rec.planet_name, 
            COALESCE(rec.installed_upgrades, 'нет улучшений');
    END LOOP;
    
    RAISE NOTICE '';
    RAISE NOTICE 'Пример 2: На каких планетах установлено конкретное улучшение?';
    FOR rec IN (
        SELECT 
            u.name as upgrade_name,
            STRING_AGG(p.name, ', ') as installed_on_planets
        FROM upgrades u
        LEFT JOIN planet_upgrades pu ON u.id = pu.upgrade_id
        LEFT JOIN planets p ON pu.planet_id = p.id
        GROUP BY u.id, u.name
        ORDER BY u.name
    ) LOOP
        RAISE NOTICE 'Улучшение "%": %', 
            rec.upgrade_name, 
            COALESCE(rec.installed_on_planets, 'ни на одной планете');
    END LOOP;
    
    RAISE NOTICE '';
    RAISE NOTICE 'Пример 3: Проверка связи M:N - одна планета может иметь несколько улучшений';
    RAISE NOTICE 'Добавим еще одно улучшение на планету 1...';
    
    -- Добавим совместимое улучшение (но сначала проверим ресурсы)
    UPDATE planets SET wealth = wealth + 10000 WHERE id = 1;
    
    INSERT INTO projects (planet_id, upgrade_id, status) VALUES (1, 1, 'PLANNED');
    UPDATE projects SET status = 'COMPLETED' WHERE planet_id = 1 AND upgrade_id = 1 AND status = 'PLANNED';
    
    RAISE NOTICE 'Теперь на планете 1 установлено улучшений: %', 
        (SELECT COUNT(*) FROM planet_upgrades WHERE planet_id = 1);
END $$;

\echo ''
\echo '=== ИТОГОВАЯ СВОДКА ==='
SELECT 
    (SELECT COUNT(*) FROM users) as users_count,
    (SELECT COUNT(*) FROM planets) as planets_count,
    (SELECT COUNT(*) FROM messages) as messages_count,
    (SELECT COUNT(*) FROM projects) as projects_count,
    (SELECT COUNT(*) FROM events WHERE NOT resolved) as active_events_count,
    (SELECT COUNT(*) FROM planet_upgrades) as planet_upgrades_count;

\echo 'Тестирование завершено!'