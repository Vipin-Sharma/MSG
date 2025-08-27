package com.jfeatures.msg.codegen.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SqlStatementDetectorTest {

    @Test
    void shouldDetectSelectStatement() throws Exception {
        String selectSql = "SELECT first_name, last_name FROM customer WHERE customer_id = ?";
        SqlStatementType result = SqlStatementDetector.detectStatementType(selectSql);
        assertThat(result).isEqualTo(SqlStatementType.SELECT);
    }

    @Test
    void shouldDetectUpdateStatement() throws Exception {
        String updateSql = "UPDATE customer SET first_name = ?, last_name = ? WHERE customer_id = ?";
        SqlStatementType result = SqlStatementDetector.detectStatementType(updateSql);
        assertThat(result).isEqualTo(SqlStatementType.UPDATE);
    }

    @Test
    void shouldDetectInsertStatement() throws Exception {
        String insertSql = "INSERT INTO customer (first_name, last_name) VALUES (?, ?)";
        SqlStatementType result = SqlStatementDetector.detectStatementType(insertSql);
        assertThat(result).isEqualTo(SqlStatementType.INSERT);
    }

    @Test
    void shouldDetectDeleteStatement() throws Exception {
        String deleteSql = "DELETE FROM customer WHERE customer_id = ?";
        SqlStatementType result = SqlStatementDetector.detectStatementType(deleteSql);
        assertThat(result).isEqualTo(SqlStatementType.DELETE);
    }

    @Test
    void shouldDetectComplexUpdateWithJoins() throws Exception {
        String complexUpdateSql = """
            UPDATE customer 
            SET customer.first_name = ?,
                customer.last_name = ?
            FROM customer 
            INNER JOIN address ON customer.address_id = address.address_id
            WHERE address.city_id = ? 
            AND customer.active = ?
            """;
        SqlStatementType result = SqlStatementDetector.detectStatementType(complexUpdateSql);
        assertThat(result).isEqualTo(SqlStatementType.UPDATE);
    }

    @Test
    void shouldDetectUpdateWithSubquery() throws Exception {
        String updateWithSubquery = """
            UPDATE customer 
            SET email = ? 
            WHERE customer_id IN (
                SELECT customer_id 
                FROM rental r 
                WHERE r.return_date IS NULL
                AND r.rental_date < ?
            )
            """;
        SqlStatementType result = SqlStatementDetector.detectStatementType(updateWithSubquery);
        assertThat(result).isEqualTo(SqlStatementType.UPDATE);
    }

    @Test
    void shouldDetectUpdateWithCaseStatement() throws Exception {
        String updateWithCase = """
            UPDATE customer 
            SET email = CASE 
                WHEN store_id = ? THEN ?
                ELSE email
            END
            WHERE customer_id = ?
            """;
        SqlStatementType result = SqlStatementDetector.detectStatementType(updateWithCase);
        assertThat(result).isEqualTo(SqlStatementType.UPDATE);
    }

    @Test
    void shouldHandleEmptyString() throws Exception {
        String emptySql = "";
        SqlStatementType result = SqlStatementDetector.detectStatementType(emptySql);
        assertThat(result).isEqualTo(SqlStatementType.UNKNOWN);
    }

    @Test
    void shouldHandleNullString() throws Exception {
        SqlStatementType result = SqlStatementDetector.detectStatementType(null);
        assertThat(result).isEqualTo(SqlStatementType.UNKNOWN);
    }

    @Test
    void shouldDetectStatementByKeywordWhenParsingFails() throws Exception {
        String malformedSql = "UPDATE customer SET incomplete syntax";
        SqlStatementType result = SqlStatementDetector.detectStatementType(malformedSql);
        assertThat(result).isEqualTo(SqlStatementType.UPDATE);
    }

    @Test
    void shouldBeCaseInsensitive() throws Exception {
        String lowerCaseUpdate = "update customer set first_name = ? where customer_id = ?";
        SqlStatementType result = SqlStatementDetector.detectStatementType(lowerCaseUpdate);
        assertThat(result).isEqualTo(SqlStatementType.UPDATE);
    }

    @Test
    void shouldHandleWhitespacePrefix() throws Exception {
        String sqlWithWhitespace = "   \n\t  UPDATE customer SET first_name = ? WHERE customer_id = ?";
        SqlStatementType result = SqlStatementDetector.detectStatementType(sqlWithWhitespace);
        assertThat(result).isEqualTo(SqlStatementType.UPDATE);
    }
}