package com.jfeatures.msg.codegen.database;

import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.config.DataSourceConfig;
import com.jfeatures.msg.config.JdbcTemplateConfig;
import com.jfeatures.msg.config.NamedParameterJdbcTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Factory for creating database connections needed for microservice generation.
 * Encapsulates the creation of DataSource, JdbcTemplate, and NamedParameterJdbcTemplate
 * using the existing configuration classes.
 */
@Slf4j
public class DatabaseConnectionFactory {
    
    /**
     * Creates a complete database connection with all required JDBC templates.
     * Uses the existing Spring configuration classes to ensure consistency
     * with the runtime application configuration.
     * 
     * @return a DatabaseConnection containing all database access components
     * @throws RuntimeException if configuration instantiation or connection setup fails
     */
    public DatabaseConnection createDatabaseConnection() {
        try {
            log.info("Creating database connection components");
            
            DataSourceConfig dataSourceConfig = new DataSourceConfig();
            JdbcTemplateConfig jdbcTemplateConfig = new JdbcTemplateConfig();
            NamedParameterJdbcTemplateConfig namedParameterJdbcTemplateConfig = new NamedParameterJdbcTemplateConfig();
            
            DataSource dataSource = dataSourceConfig.dataSource();
            JdbcTemplate jdbcTemplate = jdbcTemplateConfig.jdbcTemplate(dataSource);
            NamedParameterJdbcTemplate namedParameterJdbcTemplate = namedParameterJdbcTemplateConfig.namedParameterJdbcTemplate(dataSource);
            
            log.info("Successfully created database connection components");
            return new DatabaseConnection(dataSource, jdbcTemplate, namedParameterJdbcTemplate);
            
        } catch (RuntimeException runtimeEx) {
            log.error("Runtime exception while creating database connection: {}", runtimeEx.getMessage(), runtimeEx);
            throw new RuntimeException("Failed to create database connection due to configuration error", runtimeEx);
        } catch (Exception ex) {
            log.error("Unexpected exception while creating database connection: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to create database connection due to unexpected error", ex);
        }
    }
}