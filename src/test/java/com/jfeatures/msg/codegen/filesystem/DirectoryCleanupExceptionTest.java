package com.jfeatures.msg.codegen.filesystem;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DirectoryCleanupExceptionTest {

    @Test
    void constructorsPreserveMessage() {
        DirectoryCleanupException exception = new DirectoryCleanupException("cleanup failed");
        assertThat(exception)
            .hasMessage("cleanup failed")
            .hasNoCause();
    }

    @Test
    void constructorsPreserveCause() {
        Throwable cause = new RuntimeException("io");
        DirectoryCleanupException exception = new DirectoryCleanupException("cleanup failed", cause);
        assertThat(exception)
            .hasMessage("cleanup failed")
            .hasCause(cause);
    }
}
