package com.jfeatures.msg.codegen.database;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DatabaseConnectionException}.
 */
class DatabaseConnectionExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // given
        String message = "Connection failed";

        // when
        DatabaseConnectionException exception = new DatabaseConnectionException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        // given
        String message = "Connection failed";
        Throwable cause = new RuntimeException("Root cause");

        // when
        DatabaseConnectionException exception = new DatabaseConnectionException(message, cause);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
