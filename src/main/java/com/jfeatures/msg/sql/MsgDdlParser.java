package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.util.List;

public class MsgDdlParser {
    public static List<ColumnDefinition> parseDdl(String ddl) throws JSQLParserException {

        Statement sqlStatement = CCJSqlParserUtil.parse(ddl);
        CreateTable createTable = (CreateTable) sqlStatement;

        return createTable.getColumnDefinitions();
    }

    public static ColumnDefinition getColumnDefinition(String columnName, String ddl) throws JSQLParserException {
        Statement sqlStatement = CCJSqlParserUtil.parse(ddl);
        CreateTable createTableStatement = (CreateTable) sqlStatement;

        return createTableStatement.getColumnDefinitions()
                .stream()
                .filter(columnDefinition -> columnDefinition.getColumnName().equalsIgnoreCase(columnName))
                .findAny()
                .get();
                /*.collect(Collectors.toList())
                .get(0);*/

    }
}
