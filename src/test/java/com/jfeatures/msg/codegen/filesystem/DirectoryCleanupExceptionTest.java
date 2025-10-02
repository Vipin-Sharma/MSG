package com.jfeatures.msg.codegen.filesystem;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DirectoryCleanupException}.
 */
class DirectoryCleanupExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // given
        String message = "Cleanup failed";

        // when
        DirectoryCleanupException exception = new DirectoryCleanupException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        // given
        String message = "Cleanup failed";
        Throwable cause = new RuntimeException("Root cause");

        // when
        DirectoryCleanupException exception = new DirectoryCleanupException(message, cause);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
