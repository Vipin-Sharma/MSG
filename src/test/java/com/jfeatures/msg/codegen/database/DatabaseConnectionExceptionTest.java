package com.jfeatures.msg.codegen.database;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DatabaseConnectionExceptionTest {

    @Test
    void constructorsPreserveMessage() {
        DatabaseConnectionException exception = new DatabaseConnectionException("failure");
        assertThat(exception)
            .hasMessage("failure")
            .hasNoCause();
    }

    @Test
    void constructorsPreserveCause() {
        Throwable cause = new IllegalStateException("root");
        DatabaseConnectionException exception = new DatabaseConnectionException("failure", cause);
        assertThat(exception)
            .hasMessage("failure")
            .hasCause(cause);
    }
}
