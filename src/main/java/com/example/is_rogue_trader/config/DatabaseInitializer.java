package com.example.is_rogue_trader.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
public class DatabaseInitializer {

    private final DataSource dataSource;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initializeDatabase() {
        log.info("=== START DATABASE INITIALIZATION ===");

        try (Connection connection = dataSource.getConnection()) {
            // Выполняем каждый скрипт с детальным логированием
            //executeScriptManually(connection, "sql/reset_db.sql");
            executeScriptManually(connection, "sql/schema.sql");
            executeScriptManually(connection, "sql/functions.sql");
            executeScriptManually(connection, "sql/triggers.sql");
            executeScriptManually(connection, "sql/indexes.sql");
            //executeScriptManually(connection, "sql/data-upgrades.sql");

            log.info("=== DATABASE INITIALIZATION COMPLETED ===");

        } catch (Exception e) {
            log.error("FATAL: Database initialization error", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void executeScriptManually(Connection connection, String scriptPath) {
        log.info("--- Executing: {} ---", scriptPath);

        try {
            // Читаем весь файл
            String sql = StreamUtils.copyToString(
                    new ClassPathResource(scriptPath).getInputStream(),
                    StandardCharsets.UTF_8
            );

            // Для PL/pgSQL функций нужно разделять по $$ или по концу функции
            // Проще выполнить весь файл целиком
            try (var statement = connection.createStatement()) {
                statement.execute(sql);
            }

            log.info("✓ {} executed successfully", scriptPath);

        } catch (Exception e) {
            log.error("✗ ERROR executing {}: {}", scriptPath, e.getMessage(), e);
            throw new RuntimeException("Script execution failed: " + scriptPath, e);
        }
    }
}