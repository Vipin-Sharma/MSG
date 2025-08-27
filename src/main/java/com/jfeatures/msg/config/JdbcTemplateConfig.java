package com.jfeatures.msg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration class for JdbcTemplate.
 * Follows Vipin's Principle: One public method per class.
 */
@Configuration
public class JdbcTemplateConfig {

    /**
     * Creates and configures JdbcTemplate.
     * Single responsibility: JdbcTemplate creation only.
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}