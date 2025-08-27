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

    @Test
    void shouldDetectCteWithSelect() throws Exception {
        String cteSelectSql = """
            WITH active_customers AS (
              SELECT customer_id, first_name FROM customer WHERE active = 1
            )
            SELECT * FROM active_customers WHERE customer_id = ?
            """;
        SqlStatementType result = SqlStatementDetector.detectStatementType(cteSelectSql);
        assertThat(result).isEqualTo(SqlStatementType.SELECT);
    }

    @Test  
    void shouldDetectCteWithUpdate() throws Exception {
        String cteUpdateSql = """
            WITH customer_updates AS (
              SELECT customer_id FROM rental WHERE return_date IS NULL
            )
            UPDATE customer SET email = ? 
            WHERE customer_id IN (SELECT customer_id FROM customer_updates)
            """;
        SqlStatementType result = SqlStatementDetector.detectStatementType(cteUpdateSql);
        assertThat(result).isEqualTo(SqlStatementType.UPDATE);
    }

    @Test
    void shouldDetectCteWithDelete() throws Exception {
        String cteDeleteSql = """
            WITH old_rentals AS (
              SELECT customer_id FROM rental WHERE rental_date < ?
            )
            DELETE FROM customer 
            WHERE customer_id IN (SELECT customer_id FROM old_rentals)
            """;
        SqlStatementType result = SqlStatementDetector.detectStatementType(cteDeleteSql);
        assertThat(result).isEqualTo(SqlStatementType.DELETE);
    }

    @Test
    void shouldDetectMultipleCteWithSelect() throws Exception {
        String multipleCte = """
            WITH active_customers AS (
              SELECT customer_id, first_name FROM customer WHERE active = 1
            ),
            customer_rentals AS (
              SELECT customer_id, COUNT(*) as rental_count FROM rental GROUP BY customer_id
            )
            SELECT ac.customer_id, ac.first_name, cr.rental_count
            FROM active_customers ac
            LEFT JOIN customer_rentals cr ON ac.customer_id = cr.customer_id
            WHERE ac.customer_id = ?
            """;
        SqlStatementType result = SqlStatementDetector.detectStatementType(multipleCte);
        assertThat(result).isEqualTo(SqlStatementType.SELECT);
    }
}