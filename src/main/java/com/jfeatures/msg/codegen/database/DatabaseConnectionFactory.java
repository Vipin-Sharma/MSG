package com.jfeatures.msg.codegen.database;

import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.config.DataSourceConfig;
import com.jfeatures.msg.config.JdbcTemplateConfig;
import com.jfeatures.msg.config.NamedParameterJdbcTemplateConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Factory for creating database connections needed for microservice generation.
 * Encapsulates the creation of DataSource, JdbcTemplate, and NamedParameterJdbcTemplate
 * using the existing configuration classes.
 */
public class DatabaseConnectionFactory {
    
    /**
     * Creates a complete database connection with all required JDBC templates.
     * Uses the existing Spring configuration classes to ensure consistency
     * with the runtime application configuration.
     * 
     * @return a DatabaseConnection containing all database access components
     * @throws Exception if database connection setup fails
     */
    public DatabaseConnection createDatabaseConnection() throws Exception {
        try {
            DataSourceConfig dataSourceConfig = new DataSourceConfig();
            JdbcTemplateConfig jdbcTemplateConfig = new JdbcTemplateConfig();
            NamedParameterJdbcTemplateConfig namedParameterJdbcTemplateConfig = new NamedParameterJdbcTemplateConfig();
            
            DataSource dataSource = dataSourceConfig.dataSource();
            JdbcTemplate jdbcTemplate = jdbcTemplateConfig.jdbcTemplate(dataSource);
            NamedParameterJdbcTemplate namedParameterJdbcTemplate = namedParameterJdbcTemplateConfig.namedParameterJdbcTemplate(dataSource);
            
            return new DatabaseConnection(dataSource, jdbcTemplate, namedParameterJdbcTemplate);
        } catch (Exception e) {
            throw new Exception("Failed to create database connection: " + e.getMessage(), e);
        }
    }
}