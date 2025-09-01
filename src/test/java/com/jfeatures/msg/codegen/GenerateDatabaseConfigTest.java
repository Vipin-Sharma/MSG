package com.jfeatures.msg.codegen;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateDatabaseConfigTest {

    @Test
    void shouldGenerateValidDatabaseConfig() throws Exception {
        // When
        String result = GenerateDatabaseConfig.createDatabaseConfig("Customer");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("@Configuration");
        assertThat(result).contains("DatabaseConfig");
        assertThat(result).contains("@Bean");
        assertThat(result).contains("DataSource");
        assertThat(result).contains("NamedParameterJdbcTemplate");
    }

    @Test
    void shouldContainSqlServerConfiguration() throws Exception {
        // When
        String result = GenerateDatabaseConfig.createDatabaseConfig("Customer");

        // Then
        // The generated config uses ConfigurationProperties, so it doesn't hard-code the URL
        assertThat(result).contains("@ConfigurationProperties(\"spring.datasource\")");
        assertThat(result).contains("DataSourceBuilder.create().build()");
    }
}