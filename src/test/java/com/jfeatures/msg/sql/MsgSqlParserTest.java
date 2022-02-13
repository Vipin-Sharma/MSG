package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MsgSqlParserTest {

    private static final String tableC = "CREATE TABLE tableC (a INT, b NVARCHAR(50))";
    private static final String tableD = "CREATE TABLE tableD (c INT, d NVARCHAR(50))";
    private static final String tableE = "CREATE TABLE tableD (e INT)";

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

    @Test
    void dataTypePerColumn() throws JSQLParserException {
        String sql = "Select tableC.a, tableC.b, tableD.c, tableD.d, e from tableC as tableC, tableD as tableD, tableE";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", tableC);
        ddlPerTableName.put("tableD", tableD);
        ddlPerTableName.put("tableE", tableE);
        Map<String, ColumnDefinition> dataTypePerColumn = MsgSqlParser.dataTypePerColumn(sql, ddlPerTableName);

        System.out.println(dataTypePerColumn);
        assertEquals(5, dataTypePerColumn.size());
        assertEquals("INT", dataTypePerColumn.get("a").getColDataType().getDataType());
        assertEquals("NVARCHAR", dataTypePerColumn.get("d").getColDataType().getDataType());
        assertEquals("INT", dataTypePerColumn.get("e").getColDataType().getDataType());
    }
}
