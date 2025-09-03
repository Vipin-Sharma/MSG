package com.jfeatures.msg.codegen.dbmetadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;

class ColumnMetadataTest {

    private ColumnMetadata columnMetadata;

    @BeforeEach
    void setUp() {
        columnMetadata = new ColumnMetadata();
    }

    @Test
    void testColumnNameGetterAndSetter() {
        String testValue = "customer_id";
        columnMetadata.setColumnName(testValue);
        assertEquals(testValue, columnMetadata.getColumnName());
    }

    @Test
    void testColumnNameNullValue() {
        columnMetadata.setColumnName(null);
        assertNull(columnMetadata.getColumnName());
    }

    @Test
    void testColumnNameEmptyValue() {
        String testValue = "";
        columnMetadata.setColumnName(testValue);
        assertEquals(testValue, columnMetadata.getColumnName());
    }

    @Test
    void testColumnAliasGetterAndSetter() {
        String testValue = "id";
        columnMetadata.setColumnAlias(testValue);
        assertEquals(testValue, columnMetadata.getColumnAlias());
    }

    @Test
    void testColumnAliasNullValue() {
        columnMetadata.setColumnAlias(null);
        assertNull(columnMetadata.getColumnAlias());
    }

    @Test
    void testTableNameGetterAndSetter() {
        String testValue = "customers";
        columnMetadata.setTableName(testValue);
        assertEquals(testValue, columnMetadata.getTableName());
    }

    @Test
    void testTableNameWithSchema() {
        String testValue = "dbo.customers";
        columnMetadata.setTableName(testValue);
        assertEquals(testValue, columnMetadata.getTableName());
    }

    @Test
    void testColumnTypeGetterAndSetter() {
        int testValue = Types.VARCHAR;
        columnMetadata.setColumnType(testValue);
        assertEquals(testValue, columnMetadata.getColumnType());
    }

    @Test
    void testColumnTypeAllSqlTypes() {
        // Test various SQL types
        int[] sqlTypes = {
            Types.VARCHAR, Types.INTEGER, Types.BIGINT, Types.DECIMAL,
            Types.TIMESTAMP, Types.DATE, Types.BOOLEAN, Types.BLOB
        };
        
        for (int sqlType : sqlTypes) {
            columnMetadata.setColumnType(sqlType);
            assertEquals(sqlType, columnMetadata.getColumnType());
        }
    }

    @Test
    void testColumnTypeNameGetterAndSetter() {
        String testValue = "VARCHAR";
        columnMetadata.setColumnTypeName(testValue);
        assertEquals(testValue, columnMetadata.getColumnTypeName());
    }

    @Test
    void testColumnTypeNameVariousTypes() {
        String[] typeNames = {
            "VARCHAR", "NVARCHAR", "INT", "BIGINT", "DECIMAL", 
            "TIMESTAMP", "DATE", "BIT", "TEXT"
        };
        
        for (String typeName : typeNames) {
            columnMetadata.setColumnTypeName(typeName);
            assertEquals(typeName, columnMetadata.getColumnTypeName());
        }
    }

    @Test
    void testColumnClassNameGetterAndSetter() {
        String testValue = "java.lang.String";
        columnMetadata.setColumnClassName(testValue);
        assertEquals(testValue, columnMetadata.getColumnClassName());
    }

    @Test
    void testColumnClassNameAllJavaTypes() {
        String[] javaTypes = {
            "java.lang.String", "java.lang.Integer", "java.lang.Long",
            "java.math.BigDecimal", "java.sql.Timestamp", "java.sql.Date",
            "java.lang.Boolean", "byte[]"
        };
        
        for (String javaType : javaTypes) {
            columnMetadata.setColumnClassName(javaType);
            assertEquals(javaType, columnMetadata.getColumnClassName());
        }
    }

    @Test
    void testColumnDisplaySizeGetterAndSetter() {
        int testValue = 255;
        columnMetadata.setColumnDisplaySize(testValue);
        assertEquals(testValue, columnMetadata.getColumnDisplaySize());
    }

    @Test
    void testColumnDisplaySizeBoundaryValues() {
        // Test boundary values
        int[] sizes = {0, 1, 255, 1000, Integer.MAX_VALUE};
        
        for (int size : sizes) {
            columnMetadata.setColumnDisplaySize(size);
            assertEquals(size, columnMetadata.getColumnDisplaySize());
        }
    }

    @Test
    void testPrecisionGetterAndSetter() {
        int testValue = 18;
        columnMetadata.setPrecision(testValue);
        assertEquals(testValue, columnMetadata.getPrecision());
    }

    @Test
    void testPrecisionBoundaryValues() {
        int[] precisions = {0, 1, 18, 38, Integer.MAX_VALUE};
        
        for (int precision : precisions) {
            columnMetadata.setPrecision(precision);
            assertEquals(precision, columnMetadata.getPrecision());
        }
    }

    @Test
    void testScaleGetterAndSetter() {
        int testValue = 2;
        columnMetadata.setScale(testValue);
        assertEquals(testValue, columnMetadata.getScale());
    }

    @Test
    void testScaleBoundaryValues() {
        int[] scales = {0, 2, 4, 10, Integer.MAX_VALUE};
        
        for (int scale : scales) {
            columnMetadata.setScale(scale);
            assertEquals(scale, columnMetadata.getScale());
        }
    }

    @Test
    void testIsNullableGetterAndSetter() {
        int testValue = 1; // ResultSetMetaData.columnNullable
        columnMetadata.setIsNullable(testValue);
        assertEquals(testValue, columnMetadata.getIsNullable());
    }

    @Test
    void testIsNullableAllValues() {
        // Test all nullable constants
        int[] nullableValues = {0, 1, 2}; // columnNoNulls, columnNullable, columnNullableUnknown
        
        for (int nullableValue : nullableValues) {
            columnMetadata.setIsNullable(nullableValue);
            assertEquals(nullableValue, columnMetadata.getIsNullable());
        }
    }

    @Test
    void testIsAutoIncrementGetterAndSetter() {
        columnMetadata.setAutoIncrement(true);
        assertTrue(columnMetadata.isAutoIncrement());
        
        columnMetadata.setAutoIncrement(false);
        assertFalse(columnMetadata.isAutoIncrement());
    }

    @Test
    void testIsCaseSensitiveGetterAndSetter() {
        columnMetadata.setCaseSensitive(true);
        assertTrue(columnMetadata.isCaseSensitive());
        
        columnMetadata.setCaseSensitive(false);
        assertFalse(columnMetadata.isCaseSensitive());
    }

    @Test
    void testIsReadOnlyGetterAndSetter() {
        columnMetadata.setReadOnly(true);
        assertTrue(columnMetadata.isReadOnly());
        
        columnMetadata.setReadOnly(false);
        assertFalse(columnMetadata.isReadOnly());
    }

    @Test
    void testIsWritableGetterAndSetter() {
        columnMetadata.setWritable(true);
        assertTrue(columnMetadata.isWritable());
        
        columnMetadata.setWritable(false);
        assertFalse(columnMetadata.isWritable());
    }

    @Test
    void testIsDefinitelyWritableGetterAndSetter() {
        columnMetadata.setDefinitelyWritable(true);
        assertTrue(columnMetadata.isDefinitelyWritable());
        
        columnMetadata.setDefinitelyWritable(false);
        assertFalse(columnMetadata.isDefinitelyWritable());
    }

    @Test
    void testIsCurrencyGetterAndSetter() {
        columnMetadata.setCurrency(true);
        assertTrue(columnMetadata.isCurrency());
        
        columnMetadata.setCurrency(false);
        assertFalse(columnMetadata.isCurrency());
    }

    @Test
    void testIsSignedGetterAndSetter() {
        columnMetadata.setSigned(true);
        assertTrue(columnMetadata.isSigned());
        
        columnMetadata.setSigned(false);
        assertFalse(columnMetadata.isSigned());
    }

    @Test
    void testEqualsAndHashCode() {
        ColumnMetadata metadata1 = new ColumnMetadata();
        metadata1.setColumnName("test_column");
        metadata1.setColumnType(Types.VARCHAR);
        metadata1.setAutoIncrement(true);

        ColumnMetadata metadata2 = new ColumnMetadata();
        metadata2.setColumnName("test_column");
        metadata2.setColumnType(Types.VARCHAR);
        metadata2.setAutoIncrement(true);

        assertEquals(metadata1, metadata2);
        assertEquals(metadata1.hashCode(), metadata2.hashCode());
    }

    @Test
    void testNotEquals() {
        ColumnMetadata metadata1 = new ColumnMetadata();
        metadata1.setColumnName("test_column");

        ColumnMetadata metadata2 = new ColumnMetadata();
        metadata2.setColumnName("different_column");

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testToString() {
        columnMetadata.setColumnName("customer_id");
        columnMetadata.setColumnType(Types.INTEGER);
        columnMetadata.setTableName("customers");
        
        String toString = columnMetadata.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("customer_id"));
        assertTrue(toString.contains("customers"));
        assertTrue(toString.contains(String.valueOf(Types.INTEGER)));
    }

    @Test
    void testCompleteObjectConfiguration() {
        // Test setting all properties at once
        columnMetadata.setColumnName("order_amount");
        columnMetadata.setColumnAlias("amount");
        columnMetadata.setTableName("orders");
        columnMetadata.setColumnType(Types.DECIMAL);
        columnMetadata.setColumnTypeName("DECIMAL");
        columnMetadata.setColumnClassName("java.math.BigDecimal");
        columnMetadata.setColumnDisplaySize(10);
        columnMetadata.setPrecision(18);
        columnMetadata.setScale(2);
        columnMetadata.setIsNullable(0); // not nullable
        columnMetadata.setAutoIncrement(false);
        columnMetadata.setCaseSensitive(false);
        columnMetadata.setReadOnly(false);
        columnMetadata.setWritable(true);
        columnMetadata.setDefinitelyWritable(true);
        columnMetadata.setCurrency(true);
        columnMetadata.setSigned(true);

        // Verify all properties are set correctly
        assertEquals("order_amount", columnMetadata.getColumnName());
        assertEquals("amount", columnMetadata.getColumnAlias());
        assertEquals("orders", columnMetadata.getTableName());
        assertEquals(Types.DECIMAL, columnMetadata.getColumnType());
        assertEquals("DECIMAL", columnMetadata.getColumnTypeName());
        assertEquals("java.math.BigDecimal", columnMetadata.getColumnClassName());
        assertEquals(10, columnMetadata.getColumnDisplaySize());
        assertEquals(18, columnMetadata.getPrecision());
        assertEquals(2, columnMetadata.getScale());
        assertEquals(0, columnMetadata.getIsNullable());
        assertFalse(columnMetadata.isAutoIncrement());
        assertFalse(columnMetadata.isCaseSensitive());
        assertFalse(columnMetadata.isReadOnly());
        assertTrue(columnMetadata.isWritable());
        assertTrue(columnMetadata.isDefinitelyWritable());
        assertTrue(columnMetadata.isCurrency());
        assertTrue(columnMetadata.isSigned());
    }

    @Test
    void testDefaultValues() {
        // Test that all fields have default values (either null or false/0)
        ColumnMetadata defaultMetadata = new ColumnMetadata();
        
        assertNull(defaultMetadata.getColumnName());
        assertNull(defaultMetadata.getColumnAlias());
        assertNull(defaultMetadata.getTableName());
        assertEquals(0, defaultMetadata.getColumnType());
        assertNull(defaultMetadata.getColumnTypeName());
        assertNull(defaultMetadata.getColumnClassName());
        assertEquals(0, defaultMetadata.getColumnDisplaySize());
        assertEquals(0, defaultMetadata.getPrecision());
        assertEquals(0, defaultMetadata.getScale());
        assertEquals(0, defaultMetadata.getIsNullable());
        assertFalse(defaultMetadata.isAutoIncrement());
        assertFalse(defaultMetadata.isCaseSensitive());
        assertFalse(defaultMetadata.isReadOnly());
        assertFalse(defaultMetadata.isWritable());
        assertFalse(defaultMetadata.isDefinitelyWritable());
        assertFalse(defaultMetadata.isCurrency());
        assertFalse(defaultMetadata.isSigned());
    }
}