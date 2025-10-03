package com.jfeatures.msg.codegen.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TableColumnTest {

    @Test
    void testTableColumn_ValidConstruction() {
        TableColumn column = new TableColumn("customer_id", "id", "customers");

        assertEquals("customer_id", column.columnName());
        assertEquals("id", column.columnAliasIfAvailable());
        assertEquals("customers", column.tableName());
    }

    @Test
    void testTableColumn_NullAlias() {
        TableColumn column = new TableColumn("customer_id", null, "customers");

        assertEquals("customer_id", column.columnName());
        assertNull(column.columnAliasIfAvailable());
        assertEquals("customers", column.tableName());
    }

    @Test
    void testTableColumn_AllNullValues() {
        TableColumn column = new TableColumn(null, null, null);

        assertNull(column.columnName());
        assertNull(column.columnAliasIfAvailable());
        assertNull(column.tableName());
    }

    @Test
    void testTableColumn_EmptyStrings() {
        TableColumn column = new TableColumn("", "", "");

        assertEquals("", column.columnName());
        assertEquals("", column.columnAliasIfAvailable());
        assertEquals("", column.tableName());
    }

    @Test
    void testTableColumn_SpecialCharacters() {
        TableColumn column = new TableColumn("customer$id", "id@alias", "table#name");

        assertEquals("customer$id", column.columnName());
        assertEquals("id@alias", column.columnAliasIfAvailable());
        assertEquals("table#name", column.tableName());
    }

    @Test
    void testTableColumn_UnicodeCharacters() {
        TableColumn column = new TableColumn("顧客ID", "客戶", "テーブル");

        assertEquals("顧客ID", column.columnName());
        assertEquals("客戶", column.columnAliasIfAvailable());
        assertEquals("テーブル", column.tableName());
    }

    @Test
    void testTableColumn_VeryLongNames() {
        String longColumnName = "a".repeat(1000);
        String longAlias = "b".repeat(1000);
        String longTableName = "c".repeat(1000);

        TableColumn column = new TableColumn(longColumnName, longAlias, longTableName);

        assertEquals(longColumnName, column.columnName());
        assertEquals(longAlias, column.columnAliasIfAvailable());
        assertEquals(longTableName, column.tableName());
    }

    @Test
    void testTableColumn_WithWhitespace() {
        TableColumn column = new TableColumn("  customer_id  ", "  id  ", "  customers  ");

        assertEquals("  customer_id  ", column.columnName());
        assertEquals("  id  ", column.columnAliasIfAvailable());
        assertEquals("  customers  ", column.tableName());
    }

    @Test
    void testTableColumn_SameAliasAsColumnName() {
        TableColumn column = new TableColumn("customer_id", "customer_id", "customers");

        assertEquals("customer_id", column.columnName());
        assertEquals("customer_id", column.columnAliasIfAvailable());
        assertEquals("customers", column.tableName());
    }

    @Test
    void testTableColumn_Equals_SameValues() {
        TableColumn column1 = new TableColumn("customer_id", "id", "customers");
        TableColumn column2 = new TableColumn("customer_id", "id", "customers");

        assertEquals(column1, column2);
        assertEquals(column1.hashCode(), column2.hashCode());
    }

    @Test
    void testTableColumn_Equals_DifferentColumnName() {
        TableColumn column1 = new TableColumn("customer_id", "id", "customers");
        TableColumn column2 = new TableColumn("order_id", "id", "customers");

        assertNotEquals(column1, column2);
    }

    @Test
    void testTableColumn_Equals_DifferentAlias() {
        TableColumn column1 = new TableColumn("customer_id", "id", "customers");
        TableColumn column2 = new TableColumn("customer_id", "cust_id", "customers");

        assertNotEquals(column1, column2);
    }

    @Test
    void testTableColumn_Equals_DifferentTableName() {
        TableColumn column1 = new TableColumn("customer_id", "id", "customers");
        TableColumn column2 = new TableColumn("customer_id", "id", "orders");

        assertNotEquals(column1, column2);
    }

    @Test
    void testTableColumn_Equals_Null() {
        TableColumn column = new TableColumn("customer_id", "id", "customers");

        assertNotEquals(null, column);
    }

    @Test
    void testTableColumn_Equals_DifferentClass() {
        TableColumn column = new TableColumn("customer_id", "id", "customers");
        String other = "customer_id";

        assertNotEquals(column, other);
    }

    @Test
    void testTableColumn_ToString() {
        TableColumn column = new TableColumn("customer_id", "id", "customers");
        String toString = column.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("customer_id"));
        assertTrue(toString.contains("id"));
        assertTrue(toString.contains("customers"));
    }

    @Test
    void testTableColumn_HashCode_Consistency() {
        TableColumn column = new TableColumn("customer_id", "id", "customers");

        int hashCode1 = column.hashCode();
        int hashCode2 = column.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testTableColumn_HashCode_EqualObjects() {
        TableColumn column1 = new TableColumn("customer_id", "id", "customers");
        TableColumn column2 = new TableColumn("customer_id", "id", "customers");

        assertEquals(column1.hashCode(), column2.hashCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT", "UPDATE", "INSERT", "DELETE", "FROM", "WHERE", "JOIN"})
    void testTableColumn_SqlReservedWords(String reservedWord) {
        TableColumn column = new TableColumn(reservedWord, reservedWord, reservedWord);

        assertEquals(reservedWord, column.columnName());
        assertEquals(reservedWord, column.columnAliasIfAvailable());
        assertEquals(reservedWord, column.tableName());
    }

    @Test
    void testTableColumn_WithNumbers() {
        TableColumn column = new TableColumn("column123", "alias456", "table789");

        assertEquals("column123", column.columnName());
        assertEquals("alias456", column.columnAliasIfAvailable());
        assertEquals("table789", column.tableName());
    }

    @Test
    void testTableColumn_CamelCase() {
        TableColumn column = new TableColumn("customerId", "custId", "customerTable");

        assertEquals("customerId", column.columnName());
        assertEquals("custId", column.columnAliasIfAvailable());
        assertEquals("customerTable", column.tableName());
    }

    @Test
    void testTableColumn_SnakeCase() {
        TableColumn column = new TableColumn("customer_id", "cust_id", "customer_table");

        assertEquals("customer_id", column.columnName());
        assertEquals("cust_id", column.columnAliasIfAvailable());
        assertEquals("customer_table", column.tableName());
    }

    @Test
    void testTableColumn_MixedCase() {
        TableColumn column = new TableColumn("Customer_Id", "Cust_Id", "Customer_Table");

        assertEquals("Customer_Id", column.columnName());
        assertEquals("Cust_Id", column.columnAliasIfAvailable());
        assertEquals("Customer_Table", column.tableName());
    }

    @Test
    void testTableColumn_WithUnderscoresOnly() {
        TableColumn column = new TableColumn("___", "__", "_");

        assertEquals("___", column.columnName());
        assertEquals("__", column.columnAliasIfAvailable());
        assertEquals("_", column.tableName());
    }

    @Test
    void testTableColumn_SingleCharacter() {
        TableColumn column = new TableColumn("a", "b", "c");

        assertEquals("a", column.columnName());
        assertEquals("b", column.columnAliasIfAvailable());
        assertEquals("c", column.tableName());
    }
}
