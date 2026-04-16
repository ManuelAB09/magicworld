package com.magicworld.tfg_angular_springboot.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DatabaseSeedInitializer {

    private final DataSource dataSource;

    @Value("${app.database.seed-on-empty:true}")
    private boolean seedOnEmpty;

    @Value("${app.database.seed-script:classpath:db/migration/data.sql}")
    private Resource seedScript;

    @EventListener(ApplicationReadyEvent.class)
    public void seedDatabaseIfNeeded() {
        if (!seedOnEmpty) {
            log.info("Database seed is disabled by configuration.");
            return;
        }

        if (!isDatabaseEmpty()) {
            log.info("Database seed skipped because data already exists.");
            return;
        }

        runSeedScript();
    }

    private boolean isDatabaseEmpty() {
        try (Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection, "users")) {
                log.info("Database seed skipped because table 'users' does not exist.");
                return false;
            }
            return countRows(connection, "users") == 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to evaluate database seed condition", ex);
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        return existsInMetadata(connection, tableName)
                || existsInMetadata(connection, tableName.toUpperCase())
                || existsInMetadata(connection, tableName.toLowerCase());
    }

    private boolean existsInMetadata(Connection connection, String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData()
                .getTables(connection.getCatalog(), null, tableName, new String[] { "TABLE" })) {
            return resultSet.next();
        }
    }

    private long countRows(Connection connection, String tableName) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private void runSeedScript() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(seedScript);
        populator.setContinueOnError(false);
        populator.setIgnoreFailedDrops(true);

        try {
            DatabasePopulatorUtils.execute(populator, dataSource);
            log.info("Database seed executed from {}", seedScript);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute database seed script", ex);
        }
    }
}
