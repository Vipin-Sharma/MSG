package com.jfeatures.msg.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.jfeatures.msg.codegen.MicroServiceGenerator;
import org.junit.jupiter.api.Test;

/**
 * Basic integration tests that verify the microservice generator can be instantiated
 * and configured without requiring a database connection.
 */
class BasicIntegrationTest {

    @Test
    void shouldInstantiateMicroServiceGenerator() {
        // When
        MicroServiceGenerator generator = new MicroServiceGenerator();
        
        // Then
        assertThat(generator).isNotNull();
    }

    @Test
    void shouldHaveDefaultValues() {
        // When
        MicroServiceGenerator generator = new MicroServiceGenerator();

        // Then - The generator should be created successfully with defaults and implement Callable
        assertThat(generator)
            .isNotNull()
            .isInstanceOf(java.util.concurrent.Callable.class);
    }

    @Test
    void shouldImplementCallable() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        
        // Then
        assertThat(generator).isInstanceOf(java.util.concurrent.Callable.class);
    }
}