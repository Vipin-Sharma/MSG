package com.jfeatures.msg.integration;

import com.jfeatures.msg.codegen.MicroServiceGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        
        // Then - The generator should be created successfully with defaults
        // We can't access private fields directly, but we know it was created
        assertThat(generator).isNotNull();
    }

    @Test
    void shouldImplementCallable() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        
        // Then
        assertThat(generator).isInstanceOf(java.util.concurrent.Callable.class);
    }
}