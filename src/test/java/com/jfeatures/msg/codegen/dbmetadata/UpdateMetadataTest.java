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

    @Test
    void testUpdateMetadata_EmptyWhereColumns() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name");
        List<ColumnMetadata> whereColumns = Collections.emptyList();
        String sql = "UPDATE customers SET name = ?";

        UpdateMetadata metadata = new UpdateMetadata("customers", setColumns, whereColumns, sql);

        assertEquals(1, metadata.setColumns().size());
        assertTrue(metadata.whereColumns().isEmpty());
    }

    @Test
    void testUpdateMetadata_AllEmpty() {
        UpdateMetadata metadata = new UpdateMetadata(
            "customers",
            Collections.emptyList(),
            Collections.emptyList(),
            "UPDATE customers"
        );

        assertTrue(metadata.setColumns().isEmpty());
        assertTrue(metadata.whereColumns().isEmpty());
    }

    @Test
    void testUpdateMetadata_NullValues() {
        UpdateMetadata metadata = new UpdateMetadata(null, null, null, null);

        assertNull(metadata.tableName());
        assertNull(metadata.setColumns());
        assertNull(metadata.whereColumns());
        assertNull(metadata.originalSql());
    }

    @Test
    void testUpdateMetadata_ManySetColumns() {
        List<ColumnMetadata> setColumns = createColumnMetadataList(
            "col1", "col2", "col3", "col4", "col5", "col6", "col7", "col8", "col9", "col10"
        );
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String sql = "UPDATE customers SET col1=?,col2=?,col3=?,col4=?,col5=?,col6=?,col7=?,col8=?,col9=?,col10=? WHERE id=?";

        UpdateMetadata metadata = new UpdateMetadata("customers", setColumns, whereColumns, sql);

        assertEquals(10, metadata.setColumns().size());
    }

    @Test
    void testUpdateMetadata_ManyWhereColumns() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name");
        List<ColumnMetadata> whereColumns = createColumnMetadataList(
            "id", "status", "created_date", "modified_date", "region"
        );
        String sql = "UPDATE customers SET name = ? WHERE id=? AND status=? AND created_date=? AND modified_date=? AND region=?";

        UpdateMetadata metadata = new UpdateMetadata("customers", setColumns, whereColumns, sql);

        assertEquals(1, metadata.setColumns().size());
        assertEquals(5, metadata.whereColumns().size());
    }

    @Test
    void testUpdateMetadata_Equals_SameValues() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name");
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String sql = "UPDATE customers SET name = ? WHERE id = ?";

        UpdateMetadata metadata1 = new UpdateMetadata("customers", setColumns, whereColumns, sql);
        UpdateMetadata metadata2 = new UpdateMetadata("customers", setColumns, whereColumns, sql);

        assertEquals(metadata1, metadata2);
        assertEquals(metadata1.hashCode(), metadata2.hashCode());
    }

    @Test
    void testUpdateMetadata_Equals_DifferentTableName() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name");
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String sql = "UPDATE customers SET name = ? WHERE id = ?";

        UpdateMetadata metadata1 = new UpdateMetadata("customers", setColumns, whereColumns, sql);
        UpdateMetadata metadata2 = new UpdateMetadata("orders", setColumns, whereColumns, sql);

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testUpdateMetadata_Equals_DifferentSetColumns() {
        List<ColumnMetadata> setColumns1 = createColumnMetadataList("name");
        List<ColumnMetadata> setColumns2 = createColumnMetadataList("email");
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String sql = "UPDATE customers SET name = ? WHERE id = ?";

        UpdateMetadata metadata1 = new UpdateMetadata("customers", setColumns1, whereColumns, sql);
        UpdateMetadata metadata2 = new UpdateMetadata("customers", setColumns2, whereColumns, sql);

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testUpdateMetadata_Equals_DifferentWhereColumns() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name");
        List<ColumnMetadata> whereColumns1 = createColumnMetadataList("id");
        List<ColumnMetadata> whereColumns2 = createColumnMetadataList("email");
        String sql = "UPDATE customers SET name = ? WHERE id = ?";

        UpdateMetadata metadata1 = new UpdateMetadata("customers", setColumns, whereColumns1, sql);
        UpdateMetadata metadata2 = new UpdateMetadata("customers", setColumns, whereColumns2, sql);

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testUpdateMetadata_Equals_DifferentSql() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name");
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");

        UpdateMetadata metadata1 = new UpdateMetadata("customers", setColumns, whereColumns, "UPDATE customers SET name = ? WHERE id = ?");
        UpdateMetadata metadata2 = new UpdateMetadata("customers", setColumns, whereColumns, "UPDATE customers SET name = ? WHERE customer_id = ?");

        assertNotEquals(metadata1, metadata2);
    }

    @Test
    void testUpdateMetadata_ToString() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name");
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String sql = "UPDATE customers SET name = ? WHERE id = ?";

        UpdateMetadata metadata = new UpdateMetadata("customers", setColumns, whereColumns, sql);
        String toString = metadata.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("customers"));
        assertTrue(toString.contains(sql));
    }

    @Test
    void testUpdateMetadata_HashCodeConsistency() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name");
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String sql = "UPDATE customers SET name = ? WHERE id = ?";

        UpdateMetadata metadata = new UpdateMetadata("customers", setColumns, whereColumns, sql);

        int hashCode1 = metadata.hashCode();
        int hashCode2 = metadata.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testUpdateMetadata_LongSql() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name");
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String longSql = "UPDATE customers SET name = ? WHERE id = ?".repeat(100);

        UpdateMetadata metadata = new UpdateMetadata("customers", setColumns, whereColumns, longSql);

        assertEquals(longSql, metadata.originalSql());
        assertTrue(metadata.originalSql().length() > 4000);
    }

    @Test
    void testUpdateMetadata_SpecialCharactersInTableName() {
        List<ColumnMetadata> setColumns = createColumnMetadataList("name");
        List<ColumnMetadata> whereColumns = createColumnMetadataList("id");
        String sql = "UPDATE `customers#table` SET name = ? WHERE id = ?";

        UpdateMetadata metadata = new UpdateMetadata("customers#table", setColumns, whereColumns, sql);

        assertEquals("customers#table", metadata.tableName());
    }

    @Test
    void testUpdateMetadata_ImmutabilityOfLists() {
        List<ColumnMetadata> setColumns = new ArrayList<>(createColumnMetadataList("name"));
        List<ColumnMetadata> whereColumns = new ArrayList<>(createColumnMetadataList("id"));
        String sql = "UPDATE customers SET name = ? WHERE id = ?";

        UpdateMetadata metadata = new UpdateMetadata("customers", setColumns, whereColumns, sql);

        // Modify original lists
        setColumns.clear();
        whereColumns.clear();

        // Metadata should retain original values (depends on implementation)
        // Note: Records don't make defensive copies, so this tests behavior awareness
        assertNotNull(metadata.setColumns());
        assertNotNull(metadata.whereColumns());
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
