package com.jfeatures.msg.codegen.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TableColumnTest {

    @ParameterizedTest
    @ValueSource(strings = {"SELECT", "UPDATE", "INSERT", "DELETE", "FROM", "WHERE", "JOIN"})
    void testTableColumn_SqlReservedWords(String reservedWord) {
        TableColumn column = new TableColumn(reservedWord, reservedWord, reservedWord);

        assertEquals(reservedWord, column.columnName());
        assertEquals(reservedWord, column.columnAliasIfAvailable());
        assertEquals(reservedWord, column.tableName());
    }

    @Test
    void testTableColumn_SpecialCharacters() {
        TableColumn column = new TableColumn("customer$id", "id@alias", "table#name");

        assertEquals("customer$id", column.columnName());
        assertEquals("id@alias", column.columnAliasIfAvailable());
        assertEquals("table#name", column.tableName());
    }
}
