package com.jfeatures.msg.sql;

import com.jfeatures.msg.codegen.SQLServerDataTypeEnum;
import com.jfeatures.msg.codegen.domain.DBColumn;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class MsgDdlParser {
    public static Map<String, ColDataType> parseDdl(String ddl) throws JSQLParserException {

        Statement sqlStatement = CCJSqlParserUtil.parse(ddl);
        CreateTable createTable = (CreateTable) sqlStatement;

        return createTable.getColumnDefinitions()
                .stream()
                .collect(HashMap::new, (map, columnDefinition) -> map.put(columnDefinition.getColumnName(), columnDefinition.getColDataType()) , HashMap::putAll);
    }

    public static Optional<ColumnDefinition> getColumnDefinition(String columnName, String ddl) {
        if(ddl == null)
        {
            String errorMessage = "ddl info is not passed correctly";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        Statement sqlStatement;
        try {
            sqlStatement = CCJSqlParserUtil.parse(ddl, parser -> parser.withSquareBracketQuotation(true));
        } catch (JSQLParserException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Exception " + e.getMessage());
        }
        CreateTable createTableStatement = (CreateTable) sqlStatement;

        return createTableStatement.getColumnDefinitions()
                .stream()
                .filter(columnDefinition -> columnDefinition.getColumnName().equalsIgnoreCase(columnName))
                .findAny();

    }

    public static Optional<DBColumn> getColumnDataTypes(String columnName, String ddl) {
        Statement sqlStatement;
        try {
            sqlStatement = CCJSqlParserUtil.parse(ddl);
        } catch (JSQLParserException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Exception " + e.getMessage());
        }
        CreateTable createTableStatement = (CreateTable) sqlStatement;

        return createTableStatement.getColumnDefinitions()
                .stream()
                .filter(columnDefinition -> columnDefinition.getColumnName().equalsIgnoreCase(columnName))
                .map(columnDefinition -> new DBColumn( createTableStatement.getTable().getName(), columnName, columnDefinition.getColDataType().getDataType(),
                        SQLServerDataTypeEnum.getJdbcTypeForDBType(columnDefinition.getColDataType().getDataType())) )
                .findAny();

    }
}
