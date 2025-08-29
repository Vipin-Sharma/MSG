package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.domain.TableColumn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoFieldNameConverterTest {

    @Test
    void testConvertToJavaCamelCase_TableColumn_WithAlias_ReturnsAlias() {
        // Given
        TableColumn column = new TableColumn("customer_id", "customerId");
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(column);
        
        // Then
        assertEquals("customerId", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_TableColumn_WithoutAlias_ConvertsToCamelCase() {
        // Given
        TableColumn column = new TableColumn("customer_name", null);
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(column);
        
        // Then
        assertEquals("customerName", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_TableColumn_SnakeCaseName_ConvertsToCamelCase() {
        // Given
        TableColumn column = new TableColumn("first_name_last_name", null);
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(column);
        
        // Then
        assertEquals("firstNameLastName", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_TableColumn_SingleWord_RemainsLowercase() {
        // Given
        TableColumn column = new TableColumn("email", null);
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(column);
        
        // Then
        assertEquals("email", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_TableColumn_AlreadyCamelCase_RemainsUnchanged() {
        // Given
        TableColumn column = new TableColumn("customerName", null);
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(column);
        
        // Then
        assertEquals("customerName", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_TableColumn_EmptyAlias_ConvertsToCamelCase() {
        // Given
        TableColumn column = new TableColumn("customer_id", "");
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(column);
        
        // Then
        assertEquals("", result); // Empty alias is returned as-is
    }
    
    @Test
    void testConvertToJavaCamelCase_TableColumn_NullColumn_ThrowsException() {
        // Given
        TableColumn column = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DtoFieldNameConverter.convertToJavaCamelCase(column)
        );
        
        assertEquals("Database table column cannot be null", exception.getMessage());
    }
    
    @Test
    void testConvertToJavaCamelCase_String_ValidSnakeCase_ConvertsToCamelCase() {
        // Given
        String databaseColumnName = "customer_id";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("customerId", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_MultipleUnderscores_ConvertsToCamelCase() {
        // Given
        String databaseColumnName = "date_of_birth_timestamp";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("dateOfBirthTimestamp", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_SingleWord_RemainsLowercase() {
        // Given
        String databaseColumnName = "email";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("email", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_AlreadyCamelCase_RemainsUnchanged() {
        // Given
        String databaseColumnName = "customerName";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("customerName", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_WithNumbers_HandlesCorrectly() {
        // Given
        String databaseColumnName = "customer_id_123";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("customerId123", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_LeadingUnderscore_HandlesCorrectly() {
        // Given
        String databaseColumnName = "_customer_id";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("customerId", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_TrailingUnderscore_HandlesCorrectly() {
        // Given
        String databaseColumnName = "customer_id_";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("customerId", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_MultipleConsecutiveUnderscores_HandlesCorrectly() {
        // Given
        String databaseColumnName = "customer__id___name";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("customerIdName", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_AllUpperCase_ConvertsToCamelCase() {
        // Given
        String databaseColumnName = "CUSTOMER_ID";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("customerId", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_MixedCase_ConvertsToCamelCase() {
        // Given
        String databaseColumnName = "Customer_ID_Name";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("customerIdName", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_NullString_ThrowsException() {
        // Given
        String databaseColumnName = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName)
        );
        
        assertEquals("Database column name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testConvertToJavaCamelCase_String_EmptyString_ThrowsException() {
        // Given
        String databaseColumnName = "";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName)
        );
        
        assertEquals("Database column name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testConvertToJavaCamelCase_String_WhitespaceOnly_ThrowsException() {
        // Given
        String databaseColumnName = "   ";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName)
        );
        
        assertEquals("Database column name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testConvertToJavaCamelCase_String_OnlyUnderscores_HandlesCorrectly() {
        // Given
        String databaseColumnName = "___";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("", result); // All underscores result in empty string
    }
    
    @Test
    void testConvertToJavaCamelCase_String_SqlKeywords_ConvertsToCamelCase() {
        // Given
        String databaseColumnName = "order_by_date";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("orderByDate", result);
    }
    
    @Test
    void testConvertToJavaCamelCase_String_VeryLongName_HandlesCorrectly() {
        // Given
        String databaseColumnName = "very_long_database_column_name_with_many_words";
        
        // When
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);
        
        // Then
        assertEquals("veryLongDatabaseColumnNameWithManyWords", result);
    }
}