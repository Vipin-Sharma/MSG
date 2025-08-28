package com.jfeatures.msg.codegen.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlStatementDetectorTest {

    @Test
    void shouldDetectSelectStatement() throws Exception {
        assertThat(SqlStatementDetector.detectStatementType("SELECT * FROM customer"))
            .isEqualTo(SqlStatementType.SELECT);
    }

    @Test
    void shouldDetectInsertStatement() throws Exception {
        assertThat(SqlStatementDetector.detectStatementType("INSERT INTO customer VALUES (?)"))
            .isEqualTo(SqlStatementType.INSERT);
    }

    @Test
    void shouldDetectUpdateStatement() throws Exception {
        assertThat(SqlStatementDetector.detectStatementType("UPDATE customer SET name = ?"))
            .isEqualTo(SqlStatementType.UPDATE);
    }

    @Test
    void shouldDetectDeleteStatement() throws Exception {
        assertThat(SqlStatementDetector.detectStatementType("DELETE FROM customer"))
            .isEqualTo(SqlStatementType.DELETE);
    }

    @Test
    void shouldHandleWhitespace() throws Exception {
        assertThat(SqlStatementDetector.detectStatementType("   SELECT * FROM customer   "))
            .isEqualTo(SqlStatementType.SELECT);
    }

    @Test
    void shouldHandleCaseInsensitivity() throws Exception {
        assertThat(SqlStatementDetector.detectStatementType("select * from customer"))
            .isEqualTo(SqlStatementType.SELECT);
    }

    @Test
    void shouldReturnUnknownForNullSql() throws Exception {
        assertThat(SqlStatementDetector.detectStatementType(null))
            .isEqualTo(SqlStatementType.UNKNOWN);
    }

    @Test
    void shouldReturnUnknownForEmptySql() throws Exception {
        assertThat(SqlStatementDetector.detectStatementType(""))
            .isEqualTo(SqlStatementType.UNKNOWN);
    }

    @Test
    void shouldReturnUnknownForUnsupportedStatement() throws Exception {
        assertThat(SqlStatementDetector.detectStatementType("DROP TABLE customer"))
            .isEqualTo(SqlStatementType.UNKNOWN);
    }
}