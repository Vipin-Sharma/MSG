package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.util.HashMap;
import java.util.Map;

public class MsgDdlParser {
    public static Map<String, ColDataType> parseDdl(String ddl) throws JSQLParserException {

        Statement sqlStatement = CCJSqlParserUtil.parse(ddl);
        CreateTable createTable = (CreateTable) sqlStatement;

        return createTable.getColumnDefinitions()
                .stream()
                .collect(HashMap::new, (map, columnDefinition) -> map.put(columnDefinition.getColumnName(), columnDefinition.getColDataType()) , HashMap::putAll);
    }

    public static ColumnDefinition getColumnDefinition(String columnName, String ddl) throws JSQLParserException {
        Statement sqlStatement = CCJSqlParserUtil.parse(ddl);
        CreateTable createTableStatement = (CreateTable) sqlStatement;

        return createTableStatement.getColumnDefinitions()
                .stream()
                .filter(columnDefinition -> columnDefinition.getColumnName().equalsIgnoreCase(columnName))
                .findAny()
                .orElseThrow(() -> new RuntimeException(columnName + " does not exist in ddl"));

    }
}
