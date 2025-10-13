package com.jfeatures.msg.codegen.dbmetadata;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeleteMetadataTest {

    @Test
    void testDeleteMetadata_ValidConstruction() {
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id", "status");
        String sql = "DELETE FROM customers WHERE id = ? AND status = ?";

        DeleteMetadata metadata = new DeleteMetadata("customers", whereColumns, sql);

        assertEquals("customers", metadata.tableName());
        assertEquals(2, metadata.whereColumns().size());
        assertEquals(sql, metadata.originalSql());
    }

    @Test
    void testDeleteMetadata_ManyWhereColumns() {
        String[] columnNames = new String[20];
        for (int i = 0; i < 20; i++) {
            columnNames[i] = "column" + i;
        }
        List<ColumnMetadata> whereColumns = createColumnMetadataList(columnNames);
        String sql = "DELETE FROM customers WHERE ...";

        DeleteMetadata metadata = new DeleteMetadata("customers", whereColumns, sql);

        assertEquals(20, metadata.whereColumns().size());
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
