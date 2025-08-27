package com.jfeatures.msg.codegen.sql;

import com.jfeatures.msg.codegen.domain.DBColumn;
import org.apache.commons.text.CaseUtils;

import java.util.List;

public class SqlParameterReplacer {
    
    public static String convertToNamedParameterSql(String sql, List<DBColumn> sqlWhereClauseParameters) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }
        if (sqlWhereClauseParameters == null) {
            throw new IllegalArgumentException("SQL WHERE clause parameters list cannot be null");
        }
        
        // Count placeholders in SQL
        long placeholderCount = sql.chars().filter(ch -> ch == '?').count();
        if (placeholderCount != sqlWhereClauseParameters.size()) {
            throw new IllegalArgumentException(
                String.format("Parameter count mismatch: SQL has %d placeholders but %d parameters provided", 
                    placeholderCount, sqlWhereClauseParameters.size()));
        }
        
        String result = sql;
        for (int i = 0; i < sqlWhereClauseParameters.size(); i++) {
            DBColumn parameter = sqlWhereClauseParameters.get(i);
            if (parameter == null || parameter.columnName() == null || parameter.columnName().trim().isEmpty()) {
                throw new IllegalArgumentException(
                    String.format("Parameter at index %d has null or empty column name", i));
            }
            
            String paramName = CaseUtils.toCamelCase(parameter.columnName(), false);
            result = result.replaceFirst("\\?", ":" + paramName);
        }
        
        return result;
    }
}