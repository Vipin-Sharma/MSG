package com.jfeatures.msg.sql;

import com.jfeatures.msg.codegen.domain.TableColumn;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
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

    public static Map<TableColumn, ColumnDefinition> dataTypePerColumnWithTableInfo(String sql, Map<String, String> ddlPerTableName) throws JSQLParserException
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

        Map<TableColumn, ColumnDefinition> resultMap = new HashMap<>();

        for (int i = 0; i < columnNameList.size(); i++) {
            if(columnNameList.get(i).equals(tableAliasList.get(i)))
            {
                for (String tableName : tablesUsedInSql) {
                    Optional<ColumnDefinition> columnDefinition = MsgDdlParser.getColumnDefinition(columnNameList.get(i), ddlPerTableName.get(tableName));
                    if (columnDefinition.isPresent()) {
                        resultMap.put(new TableColumn(columnNameList.get(i), tableName), columnDefinition.get());
                        break;
                    }
                }
            }
            else {
                resultMap.put(new TableColumn(columnNameList.get(i), tableAliasList.get(i)),  MsgDdlParser.getColumnDefinition(columnNameList.get(i), ddlPerTableName.get(tableAliasList.get(i))).get());
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

    /**
     * Added method extractPredicateHavingLiterals to extract literals from having clause, that is used to create a filter
     * in SQL. Same values should be passed in json request body of controller.
     * todo: add column type as well along with name so that class that is used to create request json can be generated.
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    public static List<String> extractPredicateHavingLiterals(String sql) throws JSQLParserException {
        List<String> result = new ArrayList<>();
        Statement sqlStatement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) sqlStatement;

        String whereClause = ((PlainSelect) selectStatement.getSelectBody()).getWhere().toString();
        System.out.println("Where condition: " + whereClause);

        Expression expr = CCJSqlParserUtil.parseCondExpression(whereClause);
        expr.accept(new ExpressionVisitorAdapter() {

            @Override
            protected void visitBinaryExpression(BinaryExpression expr) {
                if (expr instanceof ComparisonOperator) {
                    if(!(expr.getLeftExpression() instanceof Column))
                    {
                        System.out.println("left=" + expr.getLeftExpression().toString() + "  op=" +  expr.getStringExpression() + "  right=" + expr.getRightExpression());
                        result.add(expr.getRightExpression().toString());
                    }
                    if(!(expr.getRightExpression() instanceof Column))
                    {
                        System.out.println("left=" + expr.getLeftExpression() + "  op=" +  expr.getStringExpression() + "  right=" + expr.getRightExpression().toString());
                        result.add(expr.getLeftExpression().toString());
                    }

                }

                super.visitBinaryExpression(expr);
            }
        });

        return result;
    }
}
