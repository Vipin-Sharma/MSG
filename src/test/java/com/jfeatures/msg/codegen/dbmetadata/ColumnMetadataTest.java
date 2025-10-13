package com.jfeatures.msg.codegen.dbmetadata;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Types;
import org.junit.jupiter.api.Test;

class ColumnMetadataTest {

    @Test
    void testCompleteObjectConfiguration() {
        // Test setting all properties at once
        ColumnMetadata columnMetadata = new ColumnMetadata();
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
}
