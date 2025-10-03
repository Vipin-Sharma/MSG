package com.jfeatures.msg.codegen.dbmetadata;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
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
    void testDeleteMetadata_SingleWhereColumn() {
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String sql = "DELETE FROM customers WHERE id = ?";

        DeleteMetadata metadata = new DeleteMetadata("customers", whereColumns, sql);

        assertEquals(1, metadata.whereColumns().size());
        assertEquals("id", metadata.whereColumns().get(0).getColumnName());
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

    @Test
    void testDeleteMetadata_NoWhereColumns() {
        DeleteMetadata metadata = new DeleteMetadata("customers", Collections.emptyList(), "DELETE FROM customers");

        assertTrue(metadata.whereColumns().isEmpty());
    }

    @Test
    void testDeleteMetadata_NullValues() {
        DeleteMetadata metadata = new DeleteMetadata(null, null, null);

        assertNull(metadata.tableName());
        assertNull(metadata.whereColumns());
        assertNull(metadata.originalSql());
    }

    @Test
    void testDeleteMetadata_Equals_SameValues() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");
        String sql = "DELETE FROM customers WHERE id = ?";

        DeleteMetadata metadata1 = new DeleteMetadata("customers", columns, sql);
        DeleteMetadata metadata2 = new DeleteMetadata("customers", columns, sql);

        assertEquals(metadata1, metadata2);
        assertEquals(metadata1.hashCode(), metadata2.hashCode());
    }

    @Test
    void testDeleteMetadata_Equals_DifferentTableName() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");
        String sql = "DELETE FROM customers WHERE id = ?";

        DeleteMetadata metadata1 = new DeleteMetadata("customers", columns, sql);
        DeleteMetadata metadata2 = new DeleteMetadata("orders", columns, sql);

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testDeleteMetadata_Equals_DifferentWhereColumns() {
        List<ColumnMetadata> columns1 = createColumnMetadataList("id");
        List<ColumnMetadata> columns2 = createColumnMetadataList("email");
        String sql = "DELETE FROM customers WHERE id = ?";

        DeleteMetadata metadata1 = new DeleteMetadata("customers", columns1, sql);
        DeleteMetadata metadata2 = new DeleteMetadata("customers", columns2, sql);

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testDeleteMetadata_Equals_DifferentSql() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");

        DeleteMetadata metadata1 = new DeleteMetadata("customers", columns, "DELETE FROM customers WHERE id = ?");
        DeleteMetadata metadata2 = new DeleteMetadata("customers", columns, "DELETE FROM customers WHERE customer_id = ?");

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testDeleteMetadata_ToString() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");
        String sql = "DELETE FROM customers WHERE id = ?";

        DeleteMetadata metadata = new DeleteMetadata("customers", columns, sql);
        String toString = metadata.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("customers"));
        assertTrue(toString.contains(sql));
    }

    @Test
    void testDeleteMetadata_HashCodeConsistency() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");
        String sql = "DELETE FROM customers WHERE id = ?";

        DeleteMetadata metadata = new DeleteMetadata("customers", columns, sql);

        assertEquals(metadata.hashCode(), metadata.hashCode());
    }

    @Test
    void testDeleteMetadata_SpecialCharactersInTableName() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");
        String sql = "DELETE FROM `customers#table` WHERE id = ?";

        DeleteMetadata metadata = new DeleteMetadata("customers#table", columns, sql);

        assertEquals("customers#table", metadata.tableName());
    }

    @Test
    void testDeleteMetadata_ComplexWhereCondition() {
        List<ColumnMetadata> columns = createColumnMetadataList("id", "status", "created_date", "region");
        String sql = "DELETE FROM customers WHERE id = ? AND status IN (?) AND created_date > ? AND region = ?";

        DeleteMetadata metadata = new DeleteMetadata("customers", columns, sql);

        assertEquals(4, metadata.whereColumns().size());
    }

    @Test
    void testDeleteMetadata_LongSql() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");
        String longSql = "DELETE FROM customers WHERE id = ?".repeat(100);

        DeleteMetadata metadata = new DeleteMetadata("customers", columns, longSql);

        // Verify the SQL is very long (at least 3000 characters which is what 100 repetitions gives)
        assertTrue(metadata.originalSql().length() >= 3000,
            "SQL length is " + metadata.originalSql().length());
    }

    @Test
    void testDeleteMetadata_EmptyTableName() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");
        String sql = "DELETE FROM WHERE id = ?";

        DeleteMetadata metadata = new DeleteMetadata("", columns, sql);

        assertEquals("", metadata.tableName());
    }

    @Test
    void testDeleteMetadata_UnicodeTableName() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");
        String sql = "DELETE FROM 顧客テーブル WHERE id = ?";

        DeleteMetadata metadata = new DeleteMetadata("顧客テーブル", columns, sql);

        assertEquals("顧客テーブル", metadata.tableName());
    }

    @Test
    void testDeleteMetadata_MultilineSQL() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");
        String sql = """
            DELETE FROM customers
            WHERE id = ?
            AND status = 'active'
            """;

        DeleteMetadata metadata = new DeleteMetadata("customers", columns, sql);

        assertEquals(sql, metadata.originalSql());
        assertTrue(metadata.originalSql().contains("\n"));
    }

    @Test
    void testDeleteMetadata_WhereWithSubquery() {
        List<ColumnMetadata> columns = createColumnMetadataList("id");
        String sql = "DELETE FROM customers WHERE id IN (SELECT customer_id FROM orders WHERE order_date > ?)";

        DeleteMetadata metadata = new DeleteMetadata("customers", columns, sql);

        assertEquals(1, metadata.whereColumns().size());
        assertTrue(metadata.originalSql().contains("SELECT"));
    }

    @Test
    void testDeleteMetadata_WhereWithBetween() {
        List<ColumnMetadata> columns = createColumnMetadataList("created_date");
        String sql = "DELETE FROM customers WHERE created_date BETWEEN ? AND ?";

        DeleteMetadata metadata = new DeleteMetadata("customers", columns, sql);

        assertNotNull(metadata.whereColumns());
    }

    @Test
    void testDeleteMetadata_WhereWithLike() {
        List<ColumnMetadata> columns = createColumnMetadataList("name");
        String sql = "DELETE FROM customers WHERE name LIKE ?";

        DeleteMetadata metadata = new DeleteMetadata("customers", columns, sql);

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
