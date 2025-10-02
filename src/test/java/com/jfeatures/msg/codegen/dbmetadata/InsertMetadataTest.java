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
    void testInsertMetadata_SingleColumn() {
        List<ColumnMetadata> insertColumns = createColumnMetadataList("name");
        String sql = "INSERT INTO customers (name) VALUES (?)";

        InsertMetadata metadata = new InsertMetadata("customers", insertColumns, sql);

        assertEquals(1, metadata.insertColumns().size());
        assertEquals("name", metadata.insertColumns().get(0).getColumnName());
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
    void testInsertMetadata_EmptyColumns() {
        InsertMetadata metadata = new InsertMetadata("customers", Collections.emptyList(), "INSERT INTO customers");

        assertTrue(metadata.insertColumns().isEmpty());
    }

    @Test
    void testInsertMetadata_NullValues() {
        InsertMetadata metadata = new InsertMetadata(null, null, null);

        assertNull(metadata.tableName());
        assertNull(metadata.insertColumns());
        assertNull(metadata.originalSql());
    }

    @Test
    void testInsertMetadata_Equals_SameValues() {
        List<ColumnMetadata> columns = createColumnMetadataList("name", "email");
        String sql = "INSERT INTO customers (name, email) VALUES (?, ?)";

        InsertMetadata metadata1 = new InsertMetadata("customers", columns, sql);
        InsertMetadata metadata2 = new InsertMetadata("customers", columns, sql);

        assertEquals(metadata1, metadata2);
        assertEquals(metadata1.hashCode(), metadata2.hashCode());
    }

    @Test
    void testInsertMetadata_Equals_DifferentTableName() {
        List<ColumnMetadata> columns = createColumnMetadataList("name");
        String sql = "INSERT INTO customers (name) VALUES (?)";

        InsertMetadata metadata1 = new InsertMetadata("customers", columns, sql);
        InsertMetadata metadata2 = new InsertMetadata("orders", columns, sql);

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testInsertMetadata_Equals_DifferentColumns() {
        List<ColumnMetadata> columns1 = createColumnMetadataList("name");
        List<ColumnMetadata> columns2 = createColumnMetadataList("email");
        String sql = "INSERT INTO customers (name) VALUES (?)";

        InsertMetadata metadata1 = new InsertMetadata("customers", columns1, sql);
        InsertMetadata metadata2 = new InsertMetadata("customers", columns2, sql);

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testInsertMetadata_Equals_DifferentSql() {
        List<ColumnMetadata> columns = createColumnMetadataList("name");

        InsertMetadata metadata1 = new InsertMetadata("customers", columns, "INSERT INTO customers (name) VALUES (?)");
        InsertMetadata metadata2 = new InsertMetadata("customers", columns, "INSERT INTO customers (name) VALUES ('test')");

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testInsertMetadata_ToString() {
        List<ColumnMetadata> columns = createColumnMetadataList("name");
        String sql = "INSERT INTO customers (name) VALUES (?)";

        InsertMetadata metadata = new InsertMetadata("customers", columns, sql);
        String toString = metadata.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("customers"));
        assertTrue(toString.contains(sql));
    }

    @Test
    void testInsertMetadata_HashCodeConsistency() {
        List<ColumnMetadata> columns = createColumnMetadataList("name");
        String sql = "INSERT INTO customers (name) VALUES (?)";

        InsertMetadata metadata = new InsertMetadata("customers", columns, sql);

        assertEquals(metadata.hashCode(), metadata.hashCode());
    }

    @Test
    void testInsertMetadata_SpecialCharactersInTableName() {
        List<ColumnMetadata> columns = createColumnMetadataList("name");
        String sql = "INSERT INTO `customers$table` (name) VALUES (?)";

        InsertMetadata metadata = new InsertMetadata("customers$table", columns, sql);

        assertEquals("customers$table", metadata.tableName());
    }

    @Test
    void testInsertMetadata_ColumnsWithSpecialCharacters() {
        List<ColumnMetadata> columns = createColumnMetadataList("customer$name", "email@address", "phone#number");
        String sql = "INSERT INTO customers (customer$name, email@address, phone#number) VALUES (?, ?, ?)";

        InsertMetadata metadata = new InsertMetadata("customers", columns, sql);

        assertEquals(3, metadata.insertColumns().size());
        assertEquals("customer$name", metadata.insertColumns().get(0).getColumnName());
    }

    @Test
    void testInsertMetadata_LongSql() {
        List<ColumnMetadata> columns = createColumnMetadataList("name");
        String longSql = "INSERT INTO customers (name) VALUES (?)".repeat(100);

        InsertMetadata metadata = new InsertMetadata("customers", columns, longSql);

        // Verify the SQL is very long (at least 3500 characters which is what 100 repetitions gives)
        assertTrue(metadata.originalSql().length() >= 3500,
            "SQL length is " + metadata.originalSql().length());
    }

    @Test
    void testInsertMetadata_EmptyTableName() {
        List<ColumnMetadata> columns = createColumnMetadataList("name");
        String sql = "INSERT INTO (name) VALUES (?)";

        InsertMetadata metadata = new InsertMetadata("", columns, sql);

        assertEquals("", metadata.tableName());
    }

    @Test
    void testInsertMetadata_UnicodeTableName() {
        List<ColumnMetadata> columns = createColumnMetadataList("name");
        String sql = "INSERT INTO 顧客テーブル (name) VALUES (?)";

        InsertMetadata metadata = new InsertMetadata("顧客テーブル", columns, sql);

        assertEquals("顧客テーブル", metadata.tableName());
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
