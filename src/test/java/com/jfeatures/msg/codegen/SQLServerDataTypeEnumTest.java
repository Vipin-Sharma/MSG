package com.jfeatures.msg.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SQLServerDataTypeEnumTest {

    @Test
    void shouldReturnIntegerForIntType() {
        assertThat(SQLServerDataTypeEnum.getClassForType("INT")).isEqualTo(Integer.class);
    }

    @Test
    void shouldReturnStringForCharType() {
        assertThat(SQLServerDataTypeEnum.getClassForType("CHAR")).isEqualTo(String.class);
    }

    @Test
    void shouldReturnStringForNvarcharType() {
        assertThat(SQLServerDataTypeEnum.getClassForType("NVARCHAR")).isEqualTo(String.class);
    }

    @Test
    void shouldReturnBigDecimalForDecimalType() {
        assertThat(SQLServerDataTypeEnum.getClassForType("DECIMAL")).isEqualTo(BigDecimal.class);
    }

    @Test
    void shouldReturnBooleanForBitType() {
        assertThat(SQLServerDataTypeEnum.getClassForType("BIT")).isEqualTo(Boolean.class);
    }

    @Test
    void shouldHandleCaseInsensitivity() {
        assertThat(SQLServerDataTypeEnum.getClassForType("int")).isEqualTo(Integer.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("INT")).isEqualTo(Integer.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("Int")).isEqualTo(Integer.class);
    }
    
    // ========== COMPREHENSIVE COVERAGE TESTS ==========
    
    @Test
    void getClassForType_WithNullInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SQLServerDataTypeEnum.getClassForType(null)
        );
        assertEquals("Database type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void getClassForType_WithEmptyInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SQLServerDataTypeEnum.getClassForType("")
        );
        assertEquals("Database type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void getClassForType_WithWhitespaceInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SQLServerDataTypeEnum.getClassForType("   \t\n  ")
        );
        assertEquals("Database type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void getClassForType_WithUnknownType_ReturnsStringClass() {
        // Test various unknown types that should fall back to String.class
        assertThat(SQLServerDataTypeEnum.getClassForType("UNKNOWN_TYPE")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("INVALID")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("CUSTOM_TYPE")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("oracle_number")).isEqualTo(String.class);
    }
    
    @Test
    void getClassForType_WithWhitespaceAroundValidType_TrimsAndReturnsCorrectClass() {
        // Test that whitespace around valid types is properly handled
        assertThat(SQLServerDataTypeEnum.getClassForType(" INT ")).isEqualTo(Integer.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("\tVARCHAR\t")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("\nBIGINT\n")).isEqualTo(Long.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("  DECIMAL  ")).isEqualTo(BigDecimal.class);
    }
    
    @Test
    void getClassForType_AllEnumValues_ReturnCorrectClasses() {
        // Test all enum values to ensure complete coverage
        assertThat(SQLServerDataTypeEnum.getClassForType("BIGINT")).isEqualTo(Long.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("BINARY")).isEqualTo(byte[].class);
        assertThat(SQLServerDataTypeEnum.getClassForType("BIT")).isEqualTo(Boolean.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("CHAR")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("DATE")).isEqualTo(java.sql.Date.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("DATETIME")).isEqualTo(java.sql.Timestamp.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("DATETIME2")).isEqualTo(java.sql.Timestamp.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("DECIMAL")).isEqualTo(BigDecimal.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("INT")).isEqualTo(Integer.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("FLOAT")).isEqualTo(Double.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("IMAGE")).isEqualTo(byte[].class);
        assertThat(SQLServerDataTypeEnum.getClassForType("MONEY")).isEqualTo(BigDecimal.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("NCHAR")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("NTEXT")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("NUMERIC")).isEqualTo(BigDecimal.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("NVARCHAR")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("REAL")).isEqualTo(Float.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("SMALLDATETIME")).isEqualTo(java.sql.Timestamp.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("SMALLINT")).isEqualTo(Integer.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("SMALLMONEY")).isEqualTo(BigDecimal.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("TEXT")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("TIME")).isEqualTo(java.sql.Time.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("TIMESTAMP")).isEqualTo(java.sql.Timestamp.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("TINYINT")).isEqualTo(Integer.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("VARCHAR")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("UNIQUEIDENTIFIER")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("VARBINARY")).isEqualTo(byte[].class);
        assertThat(SQLServerDataTypeEnum.getClassForType("XML")).isEqualTo(String.class);
    }
    
    // ========== JDBC TYPE TESTS ==========
    
    @Test
    void getJdbcTypeForDBType_WithNullInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SQLServerDataTypeEnum.getJdbcTypeForDBType(null)
        );
        assertEquals("Database type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void getJdbcTypeForDBType_WithEmptyInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SQLServerDataTypeEnum.getJdbcTypeForDBType("")
        );
        assertEquals("Database type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void getJdbcTypeForDBType_WithWhitespaceInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SQLServerDataTypeEnum.getJdbcTypeForDBType("   \t\n  ")
        );
        assertEquals("Database type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void getJdbcTypeForDBType_WithUnknownType_ReturnsVarcharDefault() {
        // Test various unknown types that should fall back to "VARCHAR"
        assertEquals("VARCHAR", SQLServerDataTypeEnum.getJdbcTypeForDBType("UNKNOWN_TYPE"));
        assertEquals("VARCHAR", SQLServerDataTypeEnum.getJdbcTypeForDBType("INVALID"));
        assertEquals("VARCHAR", SQLServerDataTypeEnum.getJdbcTypeForDBType("CUSTOM_TYPE"));
        assertEquals("VARCHAR", SQLServerDataTypeEnum.getJdbcTypeForDBType("oracle_number"));
    }
    
    @Test
    void getJdbcTypeForDBType_WithValidTypes_ReturnsCorrectJdbcTypes() {
        // Test key JDBC type mappings
        assertEquals("Long", SQLServerDataTypeEnum.getJdbcTypeForDBType("BIGINT"));
        assertEquals("Byte[]", SQLServerDataTypeEnum.getJdbcTypeForDBType("BINARY"));
        assertEquals("Boolean", SQLServerDataTypeEnum.getJdbcTypeForDBType("BIT"));
        assertEquals("String", SQLServerDataTypeEnum.getJdbcTypeForDBType("CHAR"));
        assertEquals("Date", SQLServerDataTypeEnum.getJdbcTypeForDBType("DATE"));
        assertEquals("Timestamp", SQLServerDataTypeEnum.getJdbcTypeForDBType("DATETIME"));
        assertEquals("BigDecimal", SQLServerDataTypeEnum.getJdbcTypeForDBType("DECIMAL"));
        assertEquals("Int", SQLServerDataTypeEnum.getJdbcTypeForDBType("INT"));
        assertEquals("Double", SQLServerDataTypeEnum.getJdbcTypeForDBType("FLOAT"));
        assertEquals("Float", SQLServerDataTypeEnum.getJdbcTypeForDBType("REAL"));
        assertEquals("String", SQLServerDataTypeEnum.getJdbcTypeForDBType("VARCHAR"));
        assertEquals("Time", SQLServerDataTypeEnum.getJdbcTypeForDBType("TIME"));
        assertEquals("String", SQLServerDataTypeEnum.getJdbcTypeForDBType("XML"));
    }
    
    @Test
    void getJdbcTypeForDBType_WithCaseInsensitiveInput_ReturnsCorrectTypes() {
        // Test case insensitivity
        assertEquals("Int", SQLServerDataTypeEnum.getJdbcTypeForDBType("int"));
        assertEquals("Int", SQLServerDataTypeEnum.getJdbcTypeForDBType("INT"));
        assertEquals("Int", SQLServerDataTypeEnum.getJdbcTypeForDBType("Int"));
        assertEquals("String", SQLServerDataTypeEnum.getJdbcTypeForDBType("varchar"));
        assertEquals("String", SQLServerDataTypeEnum.getJdbcTypeForDBType("VARCHAR"));
        assertEquals("String", SQLServerDataTypeEnum.getJdbcTypeForDBType("VarChar"));
    }
    
    @Test
    void getJdbcTypeForDBType_WithWhitespaceAroundValidType_TrimsAndReturnsCorrectType() {
        // Test that whitespace around valid types is properly handled
        assertEquals("Int", SQLServerDataTypeEnum.getJdbcTypeForDBType(" INT "));
        assertEquals("String", SQLServerDataTypeEnum.getJdbcTypeForDBType("\tVARCHAR\t"));
        assertEquals("Long", SQLServerDataTypeEnum.getJdbcTypeForDBType("\nBIGINT\n"));
        assertEquals("BigDecimal", SQLServerDataTypeEnum.getJdbcTypeForDBType("  DECIMAL  "));
    }
    
    // ========== ENUM GETTER METHOD TESTS ==========
    
    @Test
    void getDbDataType_ReturnsCorrectDatabaseTypeName() {
        // Test getter method for database type names
        assertEquals("INT", SQLServerDataTypeEnum.INT.getDbDataType());
        assertEquals("VARCHAR", SQLServerDataTypeEnum.VARCHAR.getDbDataType());
        assertEquals("BIGINT", SQLServerDataTypeEnum.BIGINT.getDbDataType());
        assertEquals("DECIMAL", SQLServerDataTypeEnum.DECIMAL.getDbDataType());
        assertEquals("BIT", SQLServerDataTypeEnum.BIT.getDbDataType());
        assertEquals("UNIQUEIDENTIFIER", SQLServerDataTypeEnum.UNIQUEIDENTIFIER.getDbDataType());
    }
    
    @Test
    void getJavaClassType_ReturnsCorrectJavaClass() {
        // Test getter method for Java class types
        assertEquals(Integer.class, SQLServerDataTypeEnum.INT.getJavaClassType());
        assertEquals(String.class, SQLServerDataTypeEnum.VARCHAR.getJavaClassType());
        assertEquals(Long.class, SQLServerDataTypeEnum.BIGINT.getJavaClassType());
        assertEquals(BigDecimal.class, SQLServerDataTypeEnum.DECIMAL.getJavaClassType());
        assertEquals(Boolean.class, SQLServerDataTypeEnum.BIT.getJavaClassType());
        assertEquals(byte[].class, SQLServerDataTypeEnum.BINARY.getJavaClassType());
        assertEquals(java.sql.Date.class, SQLServerDataTypeEnum.DATE.getJavaClassType());
        assertEquals(java.sql.Time.class, SQLServerDataTypeEnum.TIME.getJavaClassType());
        assertEquals(java.sql.Timestamp.class, SQLServerDataTypeEnum.TIMESTAMP.getJavaClassType());
        assertEquals(Double.class, SQLServerDataTypeEnum.FLOAT.getJavaClassType());
        assertEquals(Float.class, SQLServerDataTypeEnum.REAL.getJavaClassType());
    }
    
    @Test
    void enum_AllValues_HaveCorrectConstruction() {
        // Test that all enum values are properly constructed
        for (SQLServerDataTypeEnum enumValue : SQLServerDataTypeEnum.values()) {
            assertNotNull(enumValue.getDbDataType(), 
                "Database type should not be null for " + enumValue.name());
            assertNotNull(enumValue.getJavaClassType(), 
                "Java class type should not be null for " + enumValue.name());
            assertFalse(enumValue.getDbDataType().trim().isEmpty(), 
                "Database type should not be empty for " + enumValue.name());
        }
    }
    
    @Test
    void enum_StaticMapInitialization_WorksCorrectly() {
        // Test that the static map is properly initialized by testing a few lookups
        // This tests the static block initialization
        Class<?> intClass = SQLServerDataTypeEnum.getClassForType("INT");
        assertEquals(Integer.class, intClass);
        
        Class<?> varcharClass = SQLServerDataTypeEnum.getClassForType("VARCHAR");
        assertEquals(String.class, varcharClass);
        
        // Test that the map is case-insensitive as expected
        Class<?> lowerCaseIntClass = SQLServerDataTypeEnum.getClassForType("int");
        assertEquals(Integer.class, lowerCaseIntClass);
    }
    
    @Test
    void getClassForType_WithMixedCaseAndSpecialTypes_HandlesCorrectly() {
        // Test some special mixed-case scenarios
        assertThat(SQLServerDataTypeEnum.getClassForType("UnIqUeIdEnTiFiEr")).isEqualTo(String.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("SmAlLdAtEtImE")).isEqualTo(java.sql.Timestamp.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("VaRbInArY")).isEqualTo(byte[].class);
    }
}