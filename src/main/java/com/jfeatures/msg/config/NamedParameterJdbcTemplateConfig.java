package com.jfeatures.msg.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Configuration class for NamedParameterJdbcTemplate.
 * Follows Vipin's Principle: One public method per class.
 */
@Configuration
public class NamedParameterJdbcTemplateConfig {

    /**
     * Creates and configures NamedParameterJdbcTemplate.
     * Single responsibility: NamedParameterJdbcTemplate creation only.
     */
    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}