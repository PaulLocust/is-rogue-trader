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
    (4, 'Бунт в улье', 5, 'Недовольство условиями жизни', false);
    
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
\echo '4. Тестирование триггеров:'
\echo '==========================='
DO $$
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
    
    -- Попробуем добавить корректный проект
    BEGIN
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
            RAISE NOTICE 'Триггер сработал правильно: %', SQLERRM;
    END;
END $$;

\echo ''
\echo '5. Тестирование ограничений (CHECK constraints):'
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
    
    -- Попробуем вставить некорректный тип улучшения
    BEGIN
        INSERT INTO upgrades (name, cost_wealth, cost_industry, cost_resources, suitable_types) 
        VALUES ('Некорректное', 1000, 1000, 1000, 'INVALID_TYPE');
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
\echo '=== ИТОГОВАЯ СВОДКА ==='
SELECT 
    (SELECT COUNT(*) FROM users) as users_count,
    (SELECT COUNT(*) FROM planets) as planets_count,
    (SELECT COUNT(*) FROM messages) as messages_count,
    (SELECT COUNT(*) FROM projects) as projects_count,
    (SELECT COUNT(*) FROM events WHERE NOT resolved) as active_events_count;

\echo 'Тестирование завершено!'
