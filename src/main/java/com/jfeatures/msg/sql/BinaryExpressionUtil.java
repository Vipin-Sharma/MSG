package com.jfeatures.msg.sql;

import net.sf.jsqlparser.expression.BinaryExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BinaryExpressionUtil
{
  public static List<BinaryExpression> getIndividualBinaryExpression(BinaryExpression binaryExpression)
  {
      if(!(binaryExpression.getLeftExpression() instanceof BinaryExpression)
              && !(binaryExpression.getRightExpression() instanceof BinaryExpression))
      {
          return Collections.singletonList(binaryExpression);
      }

      List<BinaryExpression> binaryExpressionList = new ArrayList<>();
      if(binaryExpression.getLeftExpression() instanceof BinaryExpression)
      {
          binaryExpressionList.addAll(getIndividualBinaryExpression((BinaryExpression) binaryExpression.getLeftExpression()));
      }
      if(binaryExpression.getRightExpression() instanceof BinaryExpression)
      {
          binaryExpressionList.addAll(getIndividualBinaryExpression((BinaryExpression) binaryExpression.getRightExpression()));
      }

      return binaryExpressionList;
  }
}
