package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MsgSqlParser {
    public static List<String> getSelectColumns(String sql) throws JSQLParserException {
        Statement sqlStatement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) sqlStatement;

        PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
        return plainSelect.getSelectItems()
                .stream()
                .map(selectItem -> selectItem.getASTNode().jjtGetLastToken().toString())
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Map<String, ColumnDefinition> dataTypePerColumn(String sql, Map<String, String> ddlPerTableName) throws JSQLParserException
    //public static Map<String, ColDataType> dataTypePerColumn(String sql)
    {
        Statement sqlStatement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) sqlStatement;

        PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
        ArrayList<String> columnNameList = plainSelect.getSelectItems()
                .stream()
                .map(selectItem -> selectItem.getASTNode().jjtGetLastToken().toString())
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<String> tableAliasList = plainSelect.getSelectItems()
                .stream()
                .map(selectItem -> selectItem.getASTNode().jjtGetFirstToken().toString())
                .collect(Collectors.toCollection(ArrayList::new));

        List<String> tablesUsedInSql = new TablesNamesFinder().getTableList(selectStatement);

        Map<String, ColumnDefinition> resultMap = new HashMap<>();

        for (int i = 0; i < columnNameList.size(); i++) {
            if(columnNameList.get(i).equals(tableAliasList.get(i)))
            {
                for (String tableName : tablesUsedInSql) {
                    Optional<ColumnDefinition> columnDefinition = MsgDdlParser.getColumnDefinition(columnNameList.get(i), ddlPerTableName.get(tableName));
                    if (columnDefinition.isPresent()) {
                        resultMap.put(columnNameList.get(i), columnDefinition.get());
                        break;
                    }
                }
            }
            else {
                resultMap.put(columnNameList.get(i),  MsgDdlParser.getColumnDefinition(columnNameList.get(i), ddlPerTableName.get(tableAliasList.get(i))).get());
            }
        }

        return resultMap;
    }

    public static List<String> getTablesFromSQL(String sql) throws JSQLParserException {
        Statement sqlStatement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) sqlStatement;

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        return tablesNamesFinder.getTableList(selectStatement);
    }
}
