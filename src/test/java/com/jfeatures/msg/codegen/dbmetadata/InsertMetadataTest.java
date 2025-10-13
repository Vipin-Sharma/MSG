package com.jfeatures.msg.codegen.dbmetadata;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class InsertMetadataTest {

    @Test
    void testInsertMetadata_ValidConstruction() {
        List<ColumnMetadata> insertColumns = createColumnMetadataList("name", "email", "phone");
        String sql = "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?)";

        InsertMetadata metadata = new InsertMetadata("customers", insertColumns, sql);

        assertEquals("customers", metadata.tableName());
        assertEquals(3, metadata.insertColumns().size());
        assertEquals(sql, metadata.originalSql());
    }

    @Test
    void testInsertMetadata_ManyColumns() {
        String[] columnNames = new String[50];
        for (int i = 0; i < 50; i++) {
            columnNames[i] = "column" + i;
        }
        List<ColumnMetadata> insertColumns = createColumnMetadataList(columnNames);
        String sql = "INSERT INTO customers (...) VALUES (...)";

        InsertMetadata metadata = new InsertMetadata("customers", insertColumns, sql);

        assertEquals(50, metadata.insertColumns().size());
    }

    @Test
    void testInsertMetadata_MultilineSQL() {
        List<ColumnMetadata> columns = createColumnMetadataList("name", "email");
        String sql = """
            INSERT INTO customers
            (name, email)
            VALUES (?, ?)
            """;

        InsertMetadata metadata = new InsertMetadata("customers", columns, sql);

        assertEquals(sql, metadata.originalSql());
        assertTrue(metadata.originalSql().contains("\n"));
    }

    private List<ColumnMetadata> createColumnMetadataList(String... columnNames) {
        List<ColumnMetadata> columns = new ArrayList<>();
        for (String columnName : columnNames) {
            ColumnMetadata col = new ColumnMetadata();
            col.setColumnName(columnName);
            col.setColumnType(Types.VARCHAR);
            col.setColumnTypeName("VARCHAR");
            col.setIsNullable(1);
            columns.add(col);
        }
        return columns;
    }
}
