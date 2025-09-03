package com.jfeatures.msg.codegen.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionTest {

    @Mock
    private DataSource dataSource;
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Test
    void shouldCreateValidDatabaseConnection() {
        // When
        DatabaseConnection connection = new DatabaseConnection(dataSource, jdbcTemplate, namedParameterJdbcTemplate);
        
        // Then
        assertThat(connection.dataSource()).isEqualTo(dataSource);
        assertThat(connection.jdbcTemplate()).isEqualTo(jdbcTemplate);
        assertThat(connection.namedParameterJdbcTemplate()).isEqualTo(namedParameterJdbcTemplate);
    }

    @Test
    void shouldThrowExceptionForNullDataSource() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> new DatabaseConnection(null, jdbcTemplate, namedParameterJdbcTemplate));
    }

    @Test
    void shouldThrowExceptionForNullJdbcTemplate() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> new DatabaseConnection(dataSource, null, namedParameterJdbcTemplate));
    }

    @Test
    void shouldThrowExceptionForNullNamedParameterJdbcTemplate() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> new DatabaseConnection(dataSource, jdbcTemplate, null));
    }
}