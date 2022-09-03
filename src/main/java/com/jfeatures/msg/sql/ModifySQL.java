package com.jfeatures.msg.sql;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jfeatures.msg.sql.BinaryExpressionUtil.getIndividualBinaryExpression;

@Slf4j
public class ModifySQL
{
  /**
   * This method adds parameter in place of literals in where clause so that this SQL can be used in DAO class.
   * @param sql
   * @return
   * @throws JSQLParserException
   */
  public static String modifySQLToUseNamedParameter(String sql) throws JSQLParserException
  {
    Statement sqlStatement = CCJSqlParserUtil.parse(sql);
    Select selectStatement = (Select) sqlStatement;

    PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
    String result = plainSelect.toString();

    Expression whereExpression = ((PlainSelect) selectStatement.getSelectBody()).getWhere();
    Map<String, String> stringsToReplace = new HashMap<>();

    if (whereExpression != null)
    {
      String whereClause = whereExpression.toString();
      log.info("Where condition: " + whereClause);

      Expression expr = CCJSqlParserUtil.parseCondExpression(whereClause);
      expr.accept(new ExpressionVisitorAdapter()
      {
        @Override
        protected void visitBinaryExpression(BinaryExpression expr)
        {

          if (expr instanceof ComparisonOperator)
          {
            if (!(expr.getLeftExpression() instanceof Column))
            {
              log.info("left=" + expr.getLeftExpression().toString() + "  op=" + expr.getStringExpression() + "  right=" + expr.getRightExpression());

              String columnName;
              if (expr.getRightExpression().toString().contains("'"))
              {
                columnName = StringUtils.substringAfter(expr.getRightExpression().toString(), ".");
              } else
              {
                columnName = expr.getRightExpression().toString();
              }
              String namedParameter = getNamedParameterString(columnName);
              if(expr.getLeftExpression().toString().contains("HARDCODE_AS"))
              {
                stringsToReplace.put(expr.toString(), expr.getRightExpression().toString() + " = " + getHardcodedLiteralValue(expr.getLeftExpression()));
              }else
              {
                stringsToReplace.put(expr.toString(), expr.getRightExpression().toString() + " = " + namedParameter);
              }
            }
            if (!(expr.getRightExpression() instanceof Column))
            {
              log.info("left=" + expr.getLeftExpression() + "  op=" + expr.getStringExpression() + "  right=" + expr.getRightExpression().toString());
              String columnName;
              if (expr.getLeftExpression().toString().contains("."))
              {
                columnName = StringUtils.substringAfter(expr.getLeftExpression().toString(), ".");
              } else
              {
                columnName = expr.getLeftExpression().toString();
              }
              String namedParameter = getNamedParameterString(columnName);
              if(expr.getRightExpression().toString().contains("HARDCODE_AS"))
              {
                stringsToReplace.put(expr.toString(), expr.getLeftExpression().toString() + " = " + getHardcodedLiteralValue(expr.getRightExpression()));
              }else {
                stringsToReplace.put(expr.toString(), expr.getLeftExpression().toString() + " = " + namedParameter);
              }
            }

          }

          super.visitBinaryExpression(expr);
        }
      });
    }

    List<Join> joins = plainSelect.getJoins();

    if (joins != null && !joins.isEmpty())
    {
      joins.forEach(join -> {
        Collection<Expression> onExpressions = join.getOnExpressions();

        //todo what about other possible cases other than SubSelect
        if (join.getRightItem() instanceof SubSelect joinRightItem)
        {
          try
          {
            String modifiedSqlFromJoinRightItem = modifySQLToUseNamedParameter(joinRightItem.getSelectBody().toString());
            stringsToReplace.put(joinRightItem.getSelectBody().toString(), modifiedSqlFromJoinRightItem);
          } catch (JSQLParserException e)
          {
            throw new RuntimeException(e);
          }
        }

        onExpressions.forEach(
                expression -> {
                  //todo add code like we used in extractColumnsFromJoin method
                  {
                    if (expression instanceof BinaryExpression binaryExpression)
                    {
                      List<BinaryExpression> binaryExpressionList = getIndividualBinaryExpression(binaryExpression);
                      binaryExpressionList.forEach(individualBinaryExpression ->
                      {
                        Column column;
                        if (!(individualBinaryExpression.getLeftExpression() instanceof Column))
                        {
                          log.info("left=" + individualBinaryExpression.getLeftExpression().toString() + "  op=" + individualBinaryExpression.getStringExpression() + "  right=" + individualBinaryExpression.getRightExpression());
                          column = (Column) individualBinaryExpression.getRightExpression();
                          String namedParameterString = getNamedParameterString(column.getColumnName());

                          if(individualBinaryExpression.getLeftExpression().toString().contains("HARDCODE_AS"))
                          {
                            stringsToReplace.put(individualBinaryExpression.toString(), individualBinaryExpression.getLeftExpression().toString() + " = "
                                    + getHardcodedLiteralValue(individualBinaryExpression.getLeftExpression()));
                          }
                          else
                          {
                            stringsToReplace.put(individualBinaryExpression.toString(), individualBinaryExpression.getRightExpression().toString() + " = " + namedParameterString);
                          }
                        }
                        if (!(individualBinaryExpression.getRightExpression() instanceof Column))
                        {
                          log.info("left=" + individualBinaryExpression.getLeftExpression().toString() + "  op=" + individualBinaryExpression.getStringExpression() + "  right=" + individualBinaryExpression.getRightExpression());
                          column = (Column) individualBinaryExpression.getLeftExpression();
                          String namedParameterString = getNamedParameterString(column.getColumnName());

                          if(individualBinaryExpression.getRightExpression().toString().contains("HARDCODE_AS"))
                          {
                            stringsToReplace.put(individualBinaryExpression.toString(), individualBinaryExpression.getLeftExpression().toString() + " = "
                                    + getHardcodedLiteralValue(individualBinaryExpression.getRightExpression()));
                          }
                          else
                          {
                            stringsToReplace.put(individualBinaryExpression.toString(), individualBinaryExpression.getLeftExpression().toString() + " = "
                                            + namedParameterString);
                          }
                        }
                      });
                    }
                  }
                }
        );
      });
    }

    for (Map.Entry<String, String> entry : stringsToReplace.entrySet())
    {
      String key = entry.getKey();
      String value = entry.getValue();
      result = result.replace(key, value);
    }

    return result;
  }

  private static String getHardcodedLiteralValue(Expression expr)
  {
    if(expr.toString().contains("HARDCODE_AS_STRING"))
    {
      return "'" + StringUtils.substringBetween(expr.toString(), "{", "}") + "'";
    }
    return StringUtils.substringBetween(expr.toString(), "{", "}");
  }

  private static String getNamedParameterString(String columnName)
  {
    return ":" + CaseUtils.toCamelCase(columnName, false);
  }
}
