package com.jfeatures.msg.codegen.dbmetadata;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdateMetadataTest {

    @Test
    void testUpdateMetadata_ValidConstruction() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name", "email");
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String sql = "UPDATE customers SET name = ?, email = ? WHERE id = ?";

        UpdateMetadata metadata = new UpdateMetadata("customers", setColumns, whereColumns, sql);

        assertEquals("customers", metadata.tableName());
        assertEquals(2, metadata.setColumns().size());
        assertEquals(1, metadata.whereColumns().size());
        assertEquals(sql, metadata.originalSql());
    }

    @Test
    void testUpdateMetadata_EmptySetColumns() {
        List<ColumnMetadata> setColumns = Collections.emptyList();
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String sql = "UPDATE customers WHERE id = ?";

        UpdateMetadata metadata = new UpdateMetadata("customers", setColumns, whereColumns, sql);

        assertTrue(metadata.setColumns().isEmpty());
        assertEquals(1, metadata.whereColumns().size());
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
