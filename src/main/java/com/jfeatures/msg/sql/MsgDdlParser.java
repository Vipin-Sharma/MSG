package com.jfeatures.msg.sql;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MsgDdlParser {
    public static Map<String, ColDataType> parseDdl(String ddl) throws JSQLParserException {

        Statement sqlStatement = CCJSqlParserUtil.parse(ddl);
        CreateTable createTable = (CreateTable) sqlStatement;

        return createTable.getColumnDefinitions()
                .stream()
                .collect(HashMap::new, (map, columnDefinition) -> map.put(columnDefinition.getColumnName(), columnDefinition.getColDataType()) , HashMap::putAll);
    }

}
