package com.jfeatures.msg.sql;

import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.domain.TableColumn;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MsgSqlParserTest {

    private static final String tableC = "CREATE TABLE tableC (a INT, b NVARCHAR(50))";
    private static final String tableD = "CREATE TABLE tableD (c INT, d NVARCHAR(50))";
    private static final String tableE = "CREATE TABLE tableD (e INT)";
    private static final String tableF = """
            CREATE TABLE tableF (
                a INT(10)        ,
                b BIGINT       ,
                c FLOAT       ,
                d DECIMAL       ,
                e DATE       ,
                f CHAR       ,
                g VARCHAR       ,
                h TEXT       ,
                i DATETIME      
            )
            """;

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
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableD (e INT)");
        Map<String, ColumnDefinition> dataTypePerColumn = MsgSqlParser.dataTypePerColumn(sql, ddlPerTableName);

        System.out.println(dataTypePerColumn);
        assertEquals(5, dataTypePerColumn.size());
        assertEquals("INT", dataTypePerColumn.get("a").getColDataType().getDataType());
        assertEquals("NVARCHAR", dataTypePerColumn.get("d").getColDataType().getDataType());
        assertEquals("INT", dataTypePerColumn.get("e").getColDataType().getDataType());
    }

    @Test
    void dataTypePerColumnWithTableInfo() throws JSQLParserException {
        String sql = "Select tableC.a, tableC.b, tableD.c as tabDColumnC, tableD.d, e from tableC as tableC, tableD as tableD, tableE";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableD (e INT)");
        Map<TableColumn, ColumnDefinition> dataTypePerColumn = MsgSqlParser.dataTypePerColumnWithTableInfo(sql, ddlPerTableName);

        System.out.println(dataTypePerColumn);
        assertEquals(5, dataTypePerColumn.size());
        assertEquals("INT", dataTypePerColumn.get(new TableColumn("a", null, "tableC")).getColDataType().getDataType());
        assertEquals("INT", dataTypePerColumn.get(new TableColumn("c", "tabDColumnC", "tableD")).getColDataType().getDataType());
    }

    @Test
    void testSelectSQLWithWhereClause() throws JSQLParserException {
        String sql = "Select tableC.a, tableC.b, tableD.c, tableD.d, e from tableC as tableC, tableD as tableD, tableE where tableC.a = tableD.c and tableC.b = tableD.d and tableC.a = tableE.e and tableC.b = tableE.e and tableC.a = 1 and tableC.b = 'Vipin'";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableD (e INT)");
        Map<String, ColumnDefinition> dataTypePerColumn = MsgSqlParser.dataTypePerColumn(sql, ddlPerTableName);

        System.out.println(dataTypePerColumn);
        assertEquals(5, dataTypePerColumn.size());
        assertEquals("INT", dataTypePerColumn.get("a").getColDataType().getDataType());
        assertEquals("NVARCHAR", dataTypePerColumn.get("d").getColDataType().getDataType());
        assertEquals("INT", dataTypePerColumn.get("e").getColDataType().getDataType());
    }

    @Test
    void testExtractPredicateHavingLiterals() throws JSQLParserException {
        String sql = "Select tableC.a, tableC.b, tableD.c, tableD.d, e from tableC as tableC, tableD as tableD, tableE where tableC.a = tableD.c and tableC.b = tableD.d and tableC.a = tableE.e and tableC.b = tableE.e and tableC.a = 1 and tableC.b = 'Vipin'";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableE (e INT)");
        List<DBColumn> dbColumnList = MsgSqlParser.extractPredicateHavingLiteralsFromWhereClause(sql, ddlPerTableName);
        dbColumnList.forEach(System.out::println);
        //todo Add assertions like testExtractPredicateHavingLiteralsFromJoinsClause
    }

    @Test
    void testExtractPredicateHavingLiteralsFromJoinsClause() throws JSQLParserException {
        String sql = """
                        Select tableC.a, tableC.b, tableD.c, tableD.d, e
                        from tableC as tableC
                            join tableD as tableD on tableC.a = tableD.c and tableC.a = 1 and tableC.b = 'b'
                            join tableE as tableE on tableC.a = tableE.e
                """;
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableE (e INT)");
        List<DBColumn> dbColumnList = MsgSqlParser.extractPredicateHavingLiteralsFromJoinsClause(sql, ddlPerTableName);
        dbColumnList.forEach(System.out::println);
        Assertions.assertTrue(dbColumnList.stream().anyMatch(dbColumn ->
                dbColumn.tableName().equals("tableC")
                && dbColumn.columnName().equals("a")
                && dbColumn.jdbcType().equals("Int")
                && dbColumn.javaType().equals("Integer")
                ));
        Assertions.assertTrue(dbColumnList.stream().anyMatch(dbColumn ->
                dbColumn.tableName().equals("tableC")
                && dbColumn.columnName().equals("b")
                && dbColumn.jdbcType().equals("String")
                && dbColumn.javaType().equals("String")
                ));
        Assertions.assertEquals(dbColumnList.size(), 2);
    }

    @Test
    void testModifySQLToUseNamedParameter() throws JSQLParserException {
        String sql = """
                Select tableC.a, tableC.b, tableD.c, tableD.d, e ,  tableF.a, tableF.b, tableF.c, tableF.d
                from tableC as tableC, tableD as tableD, tableE, tableF as tableF
                where tableC.a = tableD.c and tableC.b = tableD.d and tableC.a = tableE.e and tableC.b = tableE.e and tableC.a = 1 and tableC.b = 'Vipin'
                and e = 100
                """;
        String modifiedSQL = MsgSqlParser.modifySQLToUseNamedParameter(sql);
        Assertions.assertTrue(modifiedSQL.contains("tableC.a = :a"));
        Assertions.assertTrue(modifiedSQL.contains("tableC.b = :b"));
        Assertions.assertTrue(modifiedSQL.contains("e = :e"));
    }

    @Test
    void testModifySQLHavingJoinClauseWithLiteralsToUseNamedParameter() throws JSQLParserException {
        String sql = """
                        Select tableC.a, tableC.b, tableD.c, tableD.d, e
                        from tableC as tableC
                            join tableD as tableD on tableC.a = tableD.c and tableC.a = 1 and tableC.b = 'b'
                            join tableE as tableE on tableC.a = tableE.e
                """;
        String modifiedSQL = MsgSqlParser.modifySQLToUseNamedParameter(sql);
        System.out.println(modifiedSQL);
        Assertions.assertTrue(modifiedSQL.contains("tableC.a = :a"));
        Assertions.assertTrue(modifiedSQL.contains("tableC.b = :b"));
    }

    @Test
    void testGetSelectListOfColumnsDataTypes() throws JSQLParserException
    {
        String sql = "Select tableC.a, tableC.b, tableD.c, tableD.d, e ,  tableF.a, tableF.b, tableF.c, tableF.d " +
                "from tableC as tableC, tableD as tableD, tableE, tableF as tableF " +
                " " +
                " where tableC.a = tableD.c and tableC.b = tableD.d and tableC.a = tableE.e and tableC.b = tableE.e and tableC.a = 1 and tableC.b = 'Vipin'";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableD (e INT)");
        ddlPerTableName.put("tableF", tableF);
        Map<TableColumn, DBColumn> listOfColumnsDataTypes = MsgSqlParser.getDetailsOfColumnsUsedInSelect(sql, ddlPerTableName);
        listOfColumnsDataTypes.keySet().forEach(tableColumn -> System.out.println(tableColumn + " " + listOfColumnsDataTypes.get(tableColumn)));
    }
}
