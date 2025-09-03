package com.jfeatures.msg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Configuration class for database DataSource.
 * Follows Vipin's Principle: One public method per class.
 */
@Configuration
public class DataSourceConfig {

    /**
     * Creates and configures the database DataSource.
     * Single responsibility: DataSource creation only.
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=sakila;encrypt=true;trustServerCertificate=true;");
        dataSource.setUsername("sa");
        dataSource.setPassword("Password@1");
        return dataSource;
    }
}