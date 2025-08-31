package com.jfeatures.msg.codegen.sql;

import com.jfeatures.msg.codegen.domain.DBColumn;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqlParameterReplacerTest {
    
    @Test
    void testConvertToNamedParameterSql_ValidInput_ReplacesPlaceholders() {
        // Given
        String sql = "SELECT * FROM customers WHERE customer_id = ? AND status = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "customer_id", "Integer", "INTEGER"),
            new DBColumn("table", "status", "String", "VARCHAR")
        );
        
        // When
        String result = SqlParameterReplacer.convertToNamedParameterSql(sql, parameters);
        
        // Then
        String expected = "SELECT * FROM customers WHERE customer_id = :customerId AND status = :status";
        assertEquals(expected, result);
    }
    
    @Test
    void testConvertToNamedParameterSql_SnakeCaseColumns_ConvertsToCamelCase() {
        // Given
        String sql = "UPDATE customers SET first_name = ?, last_name = ? WHERE customer_id = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "first_name", "String", "VARCHAR"),
            new DBColumn("table", "last_name", "String", "VARCHAR"),
            new DBColumn("table", "customer_id", "Integer", "INTEGER")
        );
        
        // When
        String result = SqlParameterReplacer.convertToNamedParameterSql(sql, parameters);
        
        // Then
        String expected = "UPDATE customers SET first_name = :firstName, last_name = :lastName WHERE customer_id = :customerId";
        assertEquals(expected, result);
    }
    
    @Test
    void testConvertToNamedParameterSql_SingleParameter_ReplacesCorrectly() {
        // Given
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "customer_id", "Integer", "INTEGER")
        );
        
        // When
        String result = SqlParameterReplacer.convertToNamedParameterSql(sql, parameters);
        
        // Then
        String expected = "DELETE FROM customers WHERE customer_id = :customerId";
        assertEquals(expected, result);
    }
    
    @Test
    void testConvertToNamedParameterSql_NoParameters_ReturnsOriginalSql() {
        // Given
        String sql = "SELECT * FROM customers";
        List<DBColumn> parameters = Collections.emptyList();
        
        // When
        String result = SqlParameterReplacer.convertToNamedParameterSql(sql, parameters);
        
        // Then
        assertEquals(sql, result);
    }
    
    @Test
    void testConvertToNamedParameterSql_ComplexSqlWithJoin_HandlesCorrectly() {
        // Given
        String sql = """
            SELECT c.customer_id, c.customer_name, o.order_id 
            FROM customers c 
            JOIN orders o ON c.customer_id = o.customer_id 
            WHERE c.customer_id = ? AND o.order_date > ? AND c.status = ?
            """;
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "customer_id", "Integer", "INTEGER"),
            new DBColumn("table", "order_date", "Date", "DATE"),
            new DBColumn("table", "status", "String", "VARCHAR")
        );
        
        // When
        String result = SqlParameterReplacer.convertToNamedParameterSql(sql, parameters);
        
        // Then
        String expected = """
            SELECT c.customer_id, c.customer_name, o.order_id 
            FROM customers c 
            JOIN orders o ON c.customer_id = o.customer_id 
            WHERE c.customer_id = :customerId AND o.order_date > :orderDate AND c.status = :status
            """;
        assertEquals(expected, result);
    }
    
    @Test
    void testConvertToNamedParameterSql_MultipleUnderscoreColumn_ConvertsProperly() {
        // Given
        String sql = "SELECT * FROM customers WHERE date_of_birth = ? AND created_at_timestamp = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "date_of_birth", "Date", "DATE"),
            new DBColumn("table", "created_at_timestamp", "Timestamp", "TIMESTAMP")
        );
        
        // When
        String result = SqlParameterReplacer.convertToNamedParameterSql(sql, parameters);
        
        // Then
        String expected = "SELECT * FROM customers WHERE date_of_birth = :dateOfBirth AND created_at_timestamp = :createdAtTimestamp";
        assertEquals(expected, result);
    }
    
    @Test
    void testConvertToNamedParameterSql_AlreadyCamelCaseColumn_RemainsUnchanged() {
        // Given
        String sql = "SELECT * FROM customers WHERE customerId = ? AND customerName = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "customerId", "Integer", "INTEGER"),
            new DBColumn("table", "customerName", "String", "VARCHAR")
        );
        
        // When
        String result = SqlParameterReplacer.convertToNamedParameterSql(sql, parameters);
        
        // Then
        String expected = "SELECT * FROM customers WHERE customerId = :customerId AND customerName = :customerName";
        assertEquals(expected, result);
    }
    
    @Test
    void testConvertToNamedParameterSql_NullSql_ThrowsException() {
        // Given
        String sql = null;
        List<DBColumn> parameters = Collections.emptyList();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SqlParameterReplacer.convertToNamedParameterSql(sql, parameters)
        );
        
        assertEquals("SQL cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testConvertToNamedParameterSql_EmptySql_ThrowsException() {
        // Given
        String sql = "   ";
        List<DBColumn> parameters = Collections.emptyList();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SqlParameterReplacer.convertToNamedParameterSql(sql, parameters)
        );
        
        assertEquals("SQL cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testConvertToNamedParameterSql_NullParametersList_ThrowsException() {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ?";
        List<DBColumn> parameters = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SqlParameterReplacer.convertToNamedParameterSql(sql, parameters)
        );
        
        assertEquals("SQL WHERE clause parameters list cannot be null", exception.getMessage());
    }
    
    @Test
    void testConvertToNamedParameterSql_MismatchedParameterCount_MorePlaceholders_ThrowsException() {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ? AND name = ? AND status = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "id", "Integer", "INTEGER"),
            new DBColumn("table", "name", "String", "VARCHAR")
            // Missing third parameter
        );
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SqlParameterReplacer.convertToNamedParameterSql(sql, parameters)
        );
        
        assertTrue(exception.getMessage().contains("Parameter count mismatch"));
        assertTrue(exception.getMessage().contains("SQL has 3 placeholders but 2 parameters provided"));
    }
    
    @Test
    void testConvertToNamedParameterSql_MismatchedParameterCount_FewerPlaceholders_ThrowsException() {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "id", "Integer", "INTEGER"),
            new DBColumn("table", "name", "String", "VARCHAR") // Extra parameter
        );
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SqlParameterReplacer.convertToNamedParameterSql(sql, parameters)
        );
        
        assertTrue(exception.getMessage().contains("Parameter count mismatch"));
        assertTrue(exception.getMessage().contains("SQL has 1 placeholders but 2 parameters provided"));
    }
    
    @Test
    void testConvertToNamedParameterSql_NullParameterInList_ThrowsException() {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ? AND name = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "id", "Integer", "INTEGER"),
            null // Null parameter
        );
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SqlParameterReplacer.convertToNamedParameterSql(sql, parameters)
        );
        
        assertTrue(exception.getMessage().contains("Parameter at index 1 has null or empty column name"));
    }
    
    @Test
    void testConvertToNamedParameterSql_EmptyColumnName_ThrowsException() {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ? AND name = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "id", "Integer", "INTEGER"),
            new DBColumn("table", "", "String", "VARCHAR") // Empty column name
        );
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SqlParameterReplacer.convertToNamedParameterSql(sql, parameters)
        );
        
        assertTrue(exception.getMessage().contains("Parameter at index 1 has null or empty column name"));
    }
    
    @Test
    void testConvertToNamedParameterSql_WhitespaceOnlyColumnName_ThrowsException() {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ? AND name = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "id", "Integer", "INTEGER"),
            new DBColumn("table", "   ", "String", "VARCHAR") // Whitespace only column name
        );
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SqlParameterReplacer.convertToNamedParameterSql(sql, parameters)
        );
        
        assertTrue(exception.getMessage().contains("Parameter at index 1 has null or empty column name"));
    }
    
    @Test
    void testConvertToNamedParameterSql_ParametersInCorrectOrder_ReplacesInOrder() {
        // Given
        String sql = "INSERT INTO customers (first_name, last_name, email) VALUES (?, ?, ?)";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "first_name", "String", "VARCHAR"),    // Should replace first ?
            new DBColumn("table", "last_name", "String", "VARCHAR"),     // Should replace second ?
            new DBColumn("table", "email", "String", "VARCHAR")          // Should replace third ?
        );
        
        // When
        String result = SqlParameterReplacer.convertToNamedParameterSql(sql, parameters);
        
        // Then
        String expected = "INSERT INTO customers (first_name, last_name, email) VALUES (:firstName, :lastName, :email)";
        assertEquals(expected, result);
    }
    
    @Test
    void testConvertToNamedParameterSql_SqlWithQuestionMarksInStrings_OnlyReplacesParameters() {
        // Given - Note: This test assumes the method should only replace parameter placeholders, not ? in strings
        String sql = "SELECT * FROM customers WHERE description LIKE '%What?%' AND id = ?";
        List<DBColumn> parameters = Arrays.asList(
            new DBColumn("table", "id", "Integer", "INTEGER")
        );
        
        // When
        String result = SqlParameterReplacer.convertToNamedParameterSql(sql, parameters);
        
        // Then - The ? in the string literal should remain, only the parameter ? should be replaced
        String expected = "SELECT * FROM customers WHERE description LIKE '%What?%' AND id = :id";
        assertEquals(expected, result);
    }
}