package com.example.is_rogue_trader.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
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
        try (Connection connection = dataSource.getConnection()) {
            log.info("Initializing database schema...");
            
            // Execute scripts in order
            executeScript(connection, "sql/schema.sql");
            executeScript(connection, "sql/functions.sql");
            executeScript(connection, "sql/triggers.sql");
            executeScript(connection, "sql/indexes.sql");
            
            log.info("Database initialization completed successfully");
        } catch (SQLException e) {
            log.error("Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void executeScript(Connection connection, String scriptPath) {
        try {
            log.info("Executing script: {}", scriptPath);
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource(scriptPath));
            populator.setContinueOnError(true);
            populator.setSeparator(";");
            populator.populate(connection);
            log.info("Script {} executed successfully", scriptPath);
        } catch (Exception e) {
            log.error("Error executing script: {}", scriptPath, e);
            // Continue with other scripts even if one fails
        }
    }
}

