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
    void getListOfTables() throws JSQLParserException {
        String sql = "Select c.a, c.b from tableC as C, tableD as d";
        List<String> tablesFromSQL = MsgSqlParser.getTablesFromSQL(sql);

        assertEquals(2, tablesFromSQL.size());
        assertEquals(tablesFromSQL.get(0), "tableC");
        assertEquals(tablesFromSQL.get(1), "tableD");

    }
    
}
