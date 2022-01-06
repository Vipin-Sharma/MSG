package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MsgSqlParserTest {

    @Test
    void getSelectColumns() throws JSQLParserException {
        String sql = "Select a, b from tableC";
        List<String> selectColumns = MsgSqlParser.getSelectColumns(sql);

        assertEquals(2, selectColumns.size());
        assertEquals("a", selectColumns.get(0));
        assertEquals("b", selectColumns.get(1));
    }

    @Test
    void getSelectColumnsWhenMoreThanOneTableIsUsedShouldReturnCorrectOutput() throws JSQLParserException {
        String sql = "Select tab1.a, tab1.b, tab2.c, tab2.d from tableC as tab1, tableD as tab2";
        List<String> selectColumns = MsgSqlParser.getSelectColumns(sql);

        assertEquals(4, selectColumns.size());
        assertEquals("a", selectColumns.get(0));
        assertEquals("b", selectColumns.get(1));
        assertEquals("c", selectColumns.get(2));
        assertEquals("d", selectColumns.get(3));
    }

    @Test
    void getListOfTables() throws JSQLParserException {
        String sql = "Select c.a, c.b from tableC as C, tableD as d";
        List<String> tablesFromSQL = MsgSqlParser.getTablesFromSQL(sql);

        assertEquals(2, tablesFromSQL.size());
        assertEquals(tablesFromSQL.get(0), "tableC");
        assertEquals(tablesFromSQL.get(1), "tableD");

    }
    
}
