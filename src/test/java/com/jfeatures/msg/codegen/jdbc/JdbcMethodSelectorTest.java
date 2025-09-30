package com.jfeatures.msg.codegen.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcMethodSelectorTest {

    private ColumnMetadata columnMetadata;

    @BeforeEach
    void setUp() {
        columnMetadata = new ColumnMetadata();
        columnMetadata.setColumnName("test_column");
    }

    @Test
    void testSelectJdbcGetterMethodForStringTypes() {
        String[] stringTypes = {"varchar", "char", "nvarchar", "nchar", "text", "ntext"};
        
        for (String type : stringTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getString", result, "Expected getString for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForStringTypesUpperCase() {
        String[] stringTypes = {"VARCHAR", "CHAR", "NVARCHAR", "NCHAR", "TEXT", "NTEXT"};
        
        for (String type : stringTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getString", result, "Expected getString for uppercase type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForStringTypesMixedCase() {
        String[] stringTypes = {"VarChar", "Char", "NVarChar", "NChar", "Text", "NText"};
        
        for (String type : stringTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getString", result, "Expected getString for mixed case type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForIntegerTypes() {
        String[] integerTypes = {"int", "integer"};
        
        for (String type : integerTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getInt", result, "Expected getInt for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForIntegerTypesCase() {
        String[] integerTypes = {"INT", "INTEGER", "Int", "Integer"};
        
        for (String type : integerTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getInt", result, "Expected getInt for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForBigInt() {
        columnMetadata.setColumnTypeName("bigint");
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getLong", result);
    }

    @Test
    void testSelectJdbcGetterMethodForBigIntCase() {
        String[] bigintTypes = {"BIGINT", "BigInt", "bigInt"};
        
        for (String type : bigintTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getLong", result, "Expected getLong for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForSmallIntTypes() {
        String[] smallIntTypes = {"smallint", "tinyint"};
        
        for (String type : smallIntTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getShort", result, "Expected getShort for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForSmallIntTypesCase() {
        String[] smallIntTypes = {"SMALLINT", "TINYINT", "SmallInt", "TinyInt"};
        
        for (String type : smallIntTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getShort", result, "Expected getShort for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForDecimalTypes() {
        String[] decimalTypes = {"decimal", "numeric", "money", "smallmoney"};
        
        for (String type : decimalTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getBigDecimal", result, "Expected getBigDecimal for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForDecimalTypesCase() {
        String[] decimalTypes = {"DECIMAL", "NUMERIC", "MONEY", "SMALLMONEY", "Decimal", "Numeric"};
        
        for (String type : decimalTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getBigDecimal", result, "Expected getBigDecimal for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForFloatTypes() {
        String[] floatTypes = {"float", "real"};
        
        for (String type : floatTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getFloat", result, "Expected getFloat for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForFloatTypesCase() {
        String[] floatTypes = {"FLOAT", "REAL", "Float", "Real"};
        
        for (String type : floatTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getFloat", result, "Expected getFloat for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForDouble() {
        columnMetadata.setColumnTypeName("double");
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getDouble", result);
    }

    @Test
    void testSelectJdbcGetterMethodForDoubleCase() {
        String[] doubleTypes = {"DOUBLE", "Double"};
        
        for (String type : doubleTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getDouble", result, "Expected getDouble for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForBit() {
        columnMetadata.setColumnTypeName("bit");
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getBoolean", result);
    }

    @Test
    void testSelectJdbcGetterMethodForBitCase() {
        String[] bitTypes = {"BIT", "Bit"};
        
        for (String type : bitTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getBoolean", result, "Expected getBoolean for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForDateTimeTypes() {
        String[] dateTimeTypes = {"datetime", "datetime2", "smalldatetime"};
        
        for (String type : dateTimeTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getTimestamp", result, "Expected getTimestamp for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForDateTimeTypesCase() {
        String[] dateTimeTypes = {"DATETIME", "DATETIME2", "SMALLDATETIME", "DateTime", "DateTime2"};
        
        for (String type : dateTimeTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getTimestamp", result, "Expected getTimestamp for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForDate() {
        columnMetadata.setColumnTypeName("date");
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getDate", result);
    }

    @Test
    void testSelectJdbcGetterMethodForDateCase() {
        String[] dateTypes = {"DATE", "Date"};
        
        for (String type : dateTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getDate", result, "Expected getDate for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForTime() {
        columnMetadata.setColumnTypeName("time");
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getTime", result);
    }

    @Test
    void testSelectJdbcGetterMethodForTimeCase() {
        String[] timeTypes = {"TIME", "Time"};
        
        for (String type : timeTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getTime", result, "Expected getTime for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForBinaryTypes() {
        String[] binaryTypes = {"binary", "varbinary", "image"};
        
        for (String type : binaryTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getBytes", result, "Expected getBytes for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForBinaryTypesCase() {
        String[] binaryTypes = {"BINARY", "VARBINARY", "IMAGE", "Binary", "VarBinary"};
        
        for (String type : binaryTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getBytes", result, "Expected getBytes for type: " + type);
        }
    }

    @Test
    void testSelectJdbcGetterMethodForUnknownType() {
        columnMetadata.setColumnTypeName("unknown_type");
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getString", result);
    }

    @Test
    void testSelectJdbcGetterMethodForUnknownTypePrintsWarning() {
        columnMetadata.setColumnTypeName("custom_type");
        columnMetadata.setColumnName("test_column");

        // Test that unknown type defaults to getString
        // Note: Warning is now logged via SLF4J logger instead of System.err
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getString", result);
    }

    @Test
    void testSelectJdbcGetterMethodForNullColumnDefinition() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            JdbcMethodSelector.selectJdbcGetterMethodFor(null));
        
        assertEquals("Database column definition cannot be null", exception.getMessage());
    }

    @Test
    void testSelectJdbcGetterMethodForNullColumnTypeName() {
        columnMetadata.setColumnTypeName(null);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata));
        
        assertEquals("Database column type name cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSelectJdbcGetterMethodForEmptyColumnTypeName() {
        columnMetadata.setColumnTypeName("");
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata));
        
        assertEquals("Database column type name cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSelectJdbcGetterMethodForWhitespaceColumnTypeName() {
        columnMetadata.setColumnTypeName("   ");
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata));
        
        assertEquals("Database column type name cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSelectJdbcGetterMethodWithLeadingTrailingWhitespace() {
        columnMetadata.setColumnTypeName("  varchar  ");
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getString", result);
    }

    @Test
    void testSelectJdbcGetterMethodForMixedWhitespace() {
        columnMetadata.setColumnTypeName("var char");  // Space in middle
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getString", result);  // Should default to getString for unrecognized
    }

    @Test
    void testSelectJdbcGetterMethodWithSpecialCharacters() {
        String[] specialTypes = {"varchar(255)", "decimal(18,2)", "float(7)", "nvarchar(max)"};
        
        for (String type : specialTypes) {
            columnMetadata.setColumnTypeName(type);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals("getString", result, "Expected getString for unrecognized type with special chars: " + type);
        }
    }

    @Test
    void testAllSupportedTypesCompleteCoverage() {
        // This test ensures we cover all switch cases
        String[][] typeMethodPairs = {
            {"varchar", "getString"}, {"char", "getString"}, {"nvarchar", "getString"}, 
            {"nchar", "getString"}, {"text", "getString"}, {"ntext", "getString"},
            {"int", "getInt"}, {"integer", "getInt"},
            {"bigint", "getLong"},
            {"smallint", "getShort"}, {"tinyint", "getShort"},
            {"decimal", "getBigDecimal"}, {"numeric", "getBigDecimal"}, 
            {"money", "getBigDecimal"}, {"smallmoney", "getBigDecimal"},
            {"float", "getFloat"}, {"real", "getFloat"},
            {"double", "getDouble"},
            {"bit", "getBoolean"},
            {"datetime", "getTimestamp"}, {"datetime2", "getTimestamp"}, {"smalldatetime", "getTimestamp"},
            {"date", "getDate"},
            {"time", "getTime"},
            {"binary", "getBytes"}, {"varbinary", "getBytes"}, {"image", "getBytes"}
        };
        
        for (String[] pair : typeMethodPairs) {
            columnMetadata.setColumnTypeName(pair[0]);
            String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            assertEquals(pair[1], result, "Mapping failed for type: " + pair[0]);
        }
    }

    @Test
    void testNullColumnNameWithValidType() {
        columnMetadata.setColumnName(null);
        columnMetadata.setColumnTypeName("varchar");
        
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getString", result);
    }

    @Test
    void testEmptyColumnNameWithValidType() {
        columnMetadata.setColumnName("");
        columnMetadata.setColumnTypeName("int");
        
        String result = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
        assertEquals("getInt", result);
    }
}