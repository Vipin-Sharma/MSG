package com.jfeatures.msg.sql;

import com.jfeatures.msg.codegen.SQLServerDataTypeEnum;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.domain.TableColumn;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.util.ArrayList;
import java.util.Collections;
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

    public static Map<String, ColumnDefinition> dataTypePerColumn(String sql, Map<String, String> ddlPerTableName) throws JSQLParserException {
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

        Map<String, ColumnDefinition> resultMap = new HashMap<>();

        for (int i = 0; i < columnNameList.size(); i++) {
            ColumnDefinition columnDefinition;
            if (isSelectColumnWithOutTableAlias(columnNameList.get(i), tableAliasList.get(i))) {
                columnDefinition = findColumnDefinitionByColumnName(ddlPerTableName, columnNameList.get(i));
            } else {
                Map<String, String> tableAliasToTableName = getAliasToTableName(plainSelect);
                String tableName = getTableName(tableAliasList.get(i), columnNameList.get(i), tableAliasToTableName, ddlPerTableName);
                columnDefinition = MsgDdlParser.getColumnDefinition(columnNameList.get(i), ddlPerTableName.get(tableName)).get();
            }
            resultMap.put(columnNameList.get(i), columnDefinition);
        }

        return resultMap;
    }

    private static ColumnDefinition findColumnDefinitionByColumnName(Map<String, String> ddlPerTableName, String columnName) {
        ColumnDefinition columnDefinition = null;
        for (String tableName : ddlPerTableName.keySet()) {
            Optional<ColumnDefinition> columnDefinitionOptional = MsgDdlParser.getColumnDefinition(columnName, ddlPerTableName.get(tableName));
            if (columnDefinitionOptional.isPresent()) {
                columnDefinition = columnDefinitionOptional.get();
                break;
            }
        }
        return columnDefinition;
    }

    public static Map<TableColumn, ColumnDefinition> dataTypePerColumnWithTableInfo(String sql, Map<String, String> ddlPerTableName) throws JSQLParserException {
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

        Map<TableColumn, ColumnDefinition> resultMap = new HashMap<>();

        for (int i = 0; i < columnNameList.size(); i++) {
            ColumnDefinition columnDefinition;
            String tableName;
            if (isSelectColumnWithOutTableAlias(columnNameList.get(i), tableAliasList.get(i))) {
                columnDefinition = findColumnDefinitionByColumnName(ddlPerTableName, columnNameList.get(i));
                tableName = findTableNameByColumnName(columnNameList.get(i), ddlPerTableName);
            } else {
                Map<String, String> tableAliasToTableName = getAliasToTableName(plainSelect);
                tableName = getTableName(tableAliasList.get(i), columnNameList.get(i), tableAliasToTableName, ddlPerTableName);
                columnDefinition = MsgDdlParser.getColumnDefinition(columnNameList.get(i), ddlPerTableName.get(tableName)).get();
            }
            resultMap.put(new TableColumn(columnNameList.get(i), tableName) , columnDefinition);

        }

        return resultMap;
    }

    private static Map<String, String> getAliasToTableName(PlainSelect plainSelect) {
        Map<String, String> tableAliasToTableName = new HashMap<>();
        plainSelect.getFromItem().accept(new FromItemVisitorAdapter() {
            @Override
            public void visit(Table table) {
                String tableName = table.getName();
                System.out.println("Table columnName: " + tableName);

                Alias tableAlias = table.getAlias();
                if (tableAlias != null) {
                    String aliasName = tableAlias.getName();
                    System.out.println("Table alias: " + aliasName);
                    tableAliasToTableName.put(aliasName, tableName);
                }
            }
        });

        plainSelect.getJoins().forEach(join -> join.getRightItem().accept(new FromItemVisitorAdapter() {
            @Override
            public void visit(Table table) {
                String tableName = table.getName();
                System.out.println("Table columnName: " + tableName);

                Alias tableAlias = table.getAlias();
                if (tableAlias != null) {
                    String aliasName = tableAlias.getName();
                    System.out.println("Table alias: " + aliasName);
                    tableAliasToTableName.put(aliasName, tableName);
                }
            }
        }));
        return tableAliasToTableName;
    }

    private static boolean isSelectColumnWithOutTableAlias(String columnName, String tableAliasOrColumnName) {
        return columnName.equals(tableAliasOrColumnName);
    }

    public static List<String> getTablesFromSQL(String sql) throws JSQLParserException {
        Statement sqlStatement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) sqlStatement;

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        return tablesNamesFinder.getTableList(selectStatement);
    }

    /**
     * Added method extractPredicateHavingLiterals to extract literals from having clause, that is used to create a filter
     * in SQL. Same values should be passed in json request body of controller.
     * @param sql
     * @param ddlPerTableName
     * @return
     * @throws JSQLParserException
     */
    public static List<DBColumn> extractPredicateHavingLiterals(String sql, Map<String, String> ddlPerTableName) throws JSQLParserException {
        List<DBColumn> result = new ArrayList<>();
        Statement sqlStatement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) sqlStatement;

        PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
        Map<String, String> tableAliasToTableName = getAliasToTableName(plainSelect);

        Expression whereExpression = ((PlainSelect) selectStatement.getSelectBody()).getWhere();

        //todo handle this null case better, try removing if condition
        if(whereExpression == null) {
            return Collections.emptyList();
        }
        String whereClause = whereExpression.toString();
        System.out.println("Where condition: " + whereClause);

        Expression expr = CCJSqlParserUtil.parseCondExpression(whereClause);
        expr.accept(new ExpressionVisitorAdapter() {

            @Override
            protected void visitBinaryExpression(BinaryExpression expr) {
                if (expr instanceof ComparisonOperator) {
                    if (!(expr.getLeftExpression() instanceof Column)) {
                        System.out.println("left=" + expr.getLeftExpression().toString() + "  op=" + expr.getStringExpression() + "  right=" + expr.getRightExpression());

                        String tableAlias = null;
                        String columnName;
                        if(expr.getRightExpression().toString().contains("'")){
                            tableAlias = StringUtils.substringBefore(expr.getRightExpression().toString(), ".");
                            columnName = StringUtils.substringAfter(expr.getRightExpression().toString(), ".");
                        }
                        else{
                            columnName = expr.getRightExpression().toString();
                        }
                        DBColumn dbColumn = getDbColumn(tableAlias, columnName, tableAliasToTableName, ddlPerTableName);
                        result.add(dbColumn);
                    }
                    if (!(expr.getRightExpression() instanceof Column)) {
                        System.out.println("left=" + expr.getLeftExpression() + "  op=" + expr.getStringExpression() + "  right=" + expr.getRightExpression().toString());
                        String tableAlias = null;
                        String columnName;
                        if(expr.getLeftExpression().toString().contains(".")){
                            tableAlias = StringUtils.substringBefore(expr.getLeftExpression().toString(), ".");
                            columnName = StringUtils.substringAfter(expr.getLeftExpression().toString(), ".");
                        }else {
                            columnName = expr.getLeftExpression().toString();
                        }
                        DBColumn dbColumn = getDbColumn(tableAlias, columnName, tableAliasToTableName, ddlPerTableName);
                        result.add(dbColumn);
                    }

                }

                super.visitBinaryExpression(expr);
            }
        });

        return result;
    }

    public static String modifySQLToUseNamedParameter(String sql) throws JSQLParserException
    {
        String result = sql;
        Statement sqlStatement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) sqlStatement;

        PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
        Map<String, String> aliasToTableName = getAliasToTableName(plainSelect);

        Expression whereExpression = ((PlainSelect) selectStatement.getSelectBody()).getWhere();

        if (whereExpression == null)
        {
            return result;
        }
        String whereClause = whereExpression.toString();
        System.out.println("Where condition: " + whereClause);

        Expression expr = CCJSqlParserUtil.parseCondExpression(whereClause);

        Map<String, String> stringsToReplace = new HashMap<>();
        expr.accept(new ExpressionVisitorAdapter() {

            @Override
            protected void visitBinaryExpression(BinaryExpression expr)
            {
                if (expr instanceof ComparisonOperator)
                {
                    if (!(expr.getLeftExpression() instanceof Column))
                    {
                        System.out.println("left=" + expr.getLeftExpression().toString() + "  op=" + expr.getStringExpression() + "  right=" + expr.getRightExpression());

                        String tableAlias = null;
                        String columnName;
                        if (expr.getRightExpression().toString().contains("'"))
                        {
                            tableAlias = StringUtils.substringBefore(expr.getRightExpression().toString(), ".");
                            columnName = StringUtils.substringAfter(expr.getRightExpression().toString(), ".");
                        }
                        else
                        {
                            columnName = expr.getRightExpression().toString();
                        }
                        String namedParameter = getNamedParameterString(aliasToTableName.get(tableAlias), columnName);
                        stringsToReplace.put(expr.toString(), expr.getRightExpression().toString() + " = " + namedParameter);
                    }
                    if (!(expr.getRightExpression() instanceof Column))
                    {
                        System.out.println("left=" + expr.getLeftExpression() + "  op=" + expr.getStringExpression() + "  right=" + expr.getRightExpression().toString());
                        String tableAlias = null;
                        String columnName;
                        if (expr.getLeftExpression().toString().contains("."))
                        {
                            tableAlias = StringUtils.substringBefore(expr.getLeftExpression().toString(), ".");
                            columnName = StringUtils.substringAfter(expr.getLeftExpression().toString(), ".");
                        }
                        else
                        {
                            columnName = expr.getLeftExpression().toString();
                        }
                        String namedParameter = getNamedParameterString(aliasToTableName.get(tableAlias), columnName);
                        stringsToReplace.put(expr.toString(), expr.getLeftExpression().toString() + " = " + namedParameter);
                    }

                }

                super.visitBinaryExpression(expr);
            }
        });

        for (Map.Entry<String, String> entry : stringsToReplace.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            result = result.replace(key, value);
        }


        return result;
    }

    private static String getNamedParameterString(String tableName, String columnName)
    {
        if(tableName == null)
        {
            return ":" + columnName;
        }
        else
        {
            return ":" + tableName + CaseUtils.toCamelCase(columnName, true);
        }
    }

    public static Map<TableColumn, DBColumn> getDetailsOfColumnsUsedInSelect(String sql, Map<String, String> ddlPerTableName) throws JSQLParserException {
        Map<TableColumn, DBColumn> result = new HashMap<>();
        Statement sqlStatement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) sqlStatement;

        PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
        Map<String, String> tableAliasToTableName = getAliasToTableName(plainSelect);

        plainSelect.getSelectItems().forEach(selectItem -> {
            String columnName = selectItem.toString();
            String tableAlias;
            DBColumn dbColumn;
            if(columnName.contains(".")){
                tableAlias = StringUtils.substringBefore(columnName, ".");
                columnName = StringUtils.substringAfter(columnName, ".");
                dbColumn = getDbColumn(tableAlias, columnName, tableAliasToTableName, ddlPerTableName);
                result.put(new TableColumn(dbColumn.columnName(), tableAliasToTableName.get(tableAlias)), dbColumn);
            }else {
                Map<TableColumn, ColumnDefinition> tableColumnColumnDefinitionMap;
                try
                {
                    tableColumnColumnDefinitionMap = dataTypePerColumnWithTableInfo(sql, ddlPerTableName);
                } catch (JSQLParserException e)
                {
                    throw new RuntimeException(e);
                }

                String finalColumnName = columnName;
                String tableName = tableColumnColumnDefinitionMap.keySet().stream()
                        .filter(tableColumn -> tableColumn.columnName().equalsIgnoreCase(finalColumnName))
                        .findAny().get().tableName();

                dbColumn = getDbColumn( null , columnName, tableAliasToTableName, ddlPerTableName);
                result.put(new TableColumn(dbColumn.columnName(), tableName), dbColumn);
            }
        });

        return result;
    }

    private static DBColumn getDbColumn(String tableAlias, String columnName, Map<String, String> tableAliasToTableName, Map<String, String> ddlPerTableName) {
        String tableName = getTableName(tableAlias, columnName, tableAliasToTableName, ddlPerTableName);
        Optional<ColumnDefinition> columnDefinition = MsgDdlParser.getColumnDefinition(columnName, ddlPerTableName.get(tableName));
        return new DBColumn(tableName ,columnName, SQLServerDataTypeEnum.getClassForType(columnDefinition.get().getColDataType().getDataType()).getSimpleName(),
                SQLServerDataTypeEnum.getJdbcTypeForDBType(columnDefinition.get().getColDataType().getDataType()));
    }

    private static String getTableName(String tableAlias, String columnName, Map<String, String> tableAliasToTableName, Map<String, String> ddlPerTableName) {
        if (tableAlias != null) {
            return tableAliasToTableName.get(tableAlias);
        }
        return findTableNameByColumnName(columnName, ddlPerTableName);
    }

    private static String findTableNameByColumnName(String columnName, Map<String, String> ddlPerTableName) {
        for (String tableName : ddlPerTableName.keySet()) {
            Optional<ColumnDefinition> columnDefinitionOptional = MsgDdlParser.getColumnDefinition(columnName, ddlPerTableName.get(tableName));
            if (columnDefinitionOptional.isPresent()) {
                return tableName;
            }
        }
        return null;
    }
}
