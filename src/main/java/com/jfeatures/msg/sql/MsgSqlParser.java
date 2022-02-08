package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.List;
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

    public static List<String> getTablesFromSQL(String sql) throws JSQLParserException {
        Statement sqlStatement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) sqlStatement;

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        return tablesNamesFinder.getTableList(selectStatement);
    }
}
