package com.jfeatures.msg.codegen.domain;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Represents all database connection components needed for microservice generation.
 * This value object encapsulates the DataSource and both JDBC templates used
 * throughout the code generation process.
 */
public record DatabaseConnection(
    DataSource dataSource,
    JdbcTemplate jdbcTemplate,
    NamedParameterJdbcTemplate namedParameterJdbcTemplate
) {
    
    public DatabaseConnection {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        if (jdbcTemplate == null) {
            throw new IllegalArgumentException("JdbcTemplate cannot be null");
        }
        if (namedParameterJdbcTemplate == null) {
            throw new IllegalArgumentException("NamedParameterJdbcTemplate cannot be null");
        }
    }
}