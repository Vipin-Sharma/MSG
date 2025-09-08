package com.jfeatures.msg.codegen.util;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.squareup.javapoet.CodeBlock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specialized builder for SQL-related code generation patterns.
 * Follows Single Responsibility Principle - only SQL construction and formatting logic.
 */
public final class SqlBuilders {
    
    private SqlBuilders() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // =========================== SQL STATEMENT BUILDERS ===========================
    
    /**
     * Builds a parameterized INSERT statement from column metadata.
     */
    public static String buildInsertSql(String tableName, List<ColumnMetadata> columns) {
        validateNotEmpty(tableName, "tableName");
        validateNotEmpty(columns, "columns");
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(tableName).append(" (");
        
        // Column names
        List<String> columnNames = columns.stream()
                .map(ColumnMetadata::getColumnName)
                .toList();
        sqlBuilder.append(String.join(", ", columnNames));
        
        sqlBuilder.append(") VALUES (");
        
        // Named parameters
        List<String> parameterNames = columns.stream()
                .map(col -> ":" + NamingConventions.parameterName(col.getColumnName()))
                .toList();
        sqlBuilder.append(String.join(", ", parameterNames));
        sqlBuilder.append(")");
        
        return formatSql(sqlBuilder.toString());
    }
    
    /**
     * Builds a parameterized UPDATE statement from column metadata.
     */
    public static String buildUpdateSql(String tableName, List<ColumnMetadata> setColumns, List<ColumnMetadata> whereColumns) {
        validateNotEmpty(tableName, "tableName");
        validateNotEmpty(setColumns, "setColumns");
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ").append(tableName).append(" SET ");
        
        // SET clause with named parameters
        List<String> setClauses = setColumns.stream()
                .map(col -> col.getColumnName() + " = :" + NamingConventions.parameterName(col.getColumnName()))
                .toList();
        sqlBuilder.append(String.join(", ", setClauses));
        
        // WHERE clause if present
        if (whereColumns != null && !whereColumns.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            List<String> whereClauses = new ArrayList<>();
            for (int i = 0; i < whereColumns.size(); i++) {
                ColumnMetadata column = whereColumns.get(i);
                String paramName = generateWhereParamName(column, i);
                String columnName = inferColumnName(column, i);
                whereClauses.add(columnName + " = :" + paramName);
            }
            sqlBuilder.append(String.join(" AND ", whereClauses));
        }
        
        return formatSql(sqlBuilder.toString());
    }
    
    /**
     * Builds a parameterized DELETE statement.
     */
    public static String buildDeleteSql(String tableName, List<ColumnMetadata> whereColumns) {
        validateNotEmpty(tableName, "tableName");
        validateNotEmpty(whereColumns, "whereColumns");
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("DELETE FROM ").append(tableName);
        
        if (!whereColumns.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            List<String> whereClauses = new ArrayList<>();
            for (int i = 0; i < whereColumns.size(); i++) {
                ColumnMetadata column = whereColumns.get(i);
                String paramName = generateWhereParamName(column, i);
                String columnName = inferColumnName(column, i);
                whereClauses.add(columnName + " = :" + paramName);
            }
            sqlBuilder.append(String.join(" AND ", whereClauses));
        }
        
        return formatSql(sqlBuilder.toString());
    }
    
    // =========================== PARAMETER MAPPING CODE BUILDERS ===========================
    
    /**
     * Builds CodeBlock for creating and populating a parameter map from DTO.
     */
    public static CodeBlock buildDtoParameterMapping(List<ColumnMetadata> columns, String dtoParameterName) {
        validateNotEmpty(columns, "columns");
        validateNotEmpty(dtoParameterName, "dtoParameterName");
        
        CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .addStatement("$T<$T, $T> paramMap = new $T<>()", 
                             Map.class, String.class, Object.class, HashMap.class);
        
        for (ColumnMetadata column : columns) {
            String paramName = NamingConventions.parameterName(column.getColumnName());
            String getterMethod = NamingConventions.getterMethodName(column.getColumnName());
            
            codeBuilder.addStatement("paramMap.put($S, $L.$L())", 
                                   paramName, dtoParameterName, getterMethod);
        }
        
        return codeBuilder.build();
    }
    
    /**
     * Builds CodeBlock for creating parameter map from individual parameters.
     */
    public static CodeBlock buildDirectParameterMapping(List<String> paramNames, List<String> paramValues) {
        if (paramNames == null || paramValues == null || paramNames.size() != paramValues.size()) {
            throw new IllegalArgumentException("Parameter names and values must be non-null and same size");
        }
        
        CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .addStatement("$T<$T, $T> paramMap = new $T<>()", 
                             Map.class, String.class, Object.class, HashMap.class);
        
        for (int i = 0; i < paramNames.size(); i++) {
            codeBuilder.addStatement("paramMap.put($S, $L)", paramNames.get(i), paramValues.get(i));
        }
        
        return codeBuilder.build();
    }
    
    /**
     * Builds CodeBlock for mixed parameter mapping (DTO + individual parameters).
     */
    public static CodeBlock buildMixedParameterMapping(List<ColumnMetadata> dtoColumns, String dtoParameterName,
                                                      List<String> additionalParamNames, List<String> additionalParamValues) {
        validateNotEmpty(dtoColumns, "dtoColumns");
        validateNotEmpty(dtoParameterName, "dtoParameterName");
        
        CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .addStatement("$T<$T, $T> paramMap = new $T<>()", 
                             Map.class, String.class, Object.class, HashMap.class);
        
        // Add DTO parameters
        for (ColumnMetadata column : dtoColumns) {
            String paramName = NamingConventions.parameterName(column.getColumnName());
            String getterMethod = NamingConventions.getterMethodName(column.getColumnName());
            
            codeBuilder.addStatement("paramMap.put($S, $L.$L())", 
                                   paramName, dtoParameterName, getterMethod);
        }
        
        // Add additional parameters
        if (additionalParamNames != null && additionalParamValues != null && 
            additionalParamNames.size() == additionalParamValues.size()) {
            for (int i = 0; i < additionalParamNames.size(); i++) {
                codeBuilder.addStatement("paramMap.put($S, $L)", 
                                       additionalParamNames.get(i), additionalParamValues.get(i));
            }
        }
        
        return codeBuilder.build();
    }
    
    // =========================== JDBC EXECUTION CODE BUILDERS ===========================
    
    /**
     * Builds CodeBlock for JDBC template query execution.
     */
    public static CodeBlock buildQueryExecution(String jdbcTemplateField, String sqlConstant, boolean withLogging) {
        validateNotEmpty(jdbcTemplateField, "jdbcTemplateField");
        validateNotEmpty(sqlConstant, "sqlConstant");
        
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        
        if (withLogging) {
            codeBuilder.addStatement("log.info(\"Executing query: {}\", $L)", sqlConstant)
                       .addStatement("log.debug(\"Parameters: {}\", paramMap)")
                       .add("\n");
        }
        
        codeBuilder.addStatement("return $N.query($L, paramMap, rowMapper)", jdbcTemplateField, sqlConstant);
        
        return codeBuilder.build();
    }
    
    /**
     * Builds CodeBlock for JDBC template update execution.
     */
    public static CodeBlock buildUpdateExecution(String jdbcTemplateField, String sqlConstant, String operation, boolean withLogging) {
        validateNotEmpty(jdbcTemplateField, "jdbcTemplateField");
        validateNotEmpty(sqlConstant, "sqlConstant");
        validateNotEmpty(operation, "operation");
        
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        
        if (withLogging) {
            codeBuilder.addStatement("log.info(\"Executing $L: {}\", $L)", operation.toUpperCase(), sqlConstant)
                       .addStatement("log.debug(\"Parameters: {}\", paramMap)")
                       .add("\n");
        }
        
        codeBuilder.addStatement("int rowsAffected = $N.update($L, paramMap)", jdbcTemplateField, sqlConstant);
        
        if (withLogging) {
            codeBuilder.addStatement("log.info(\"$L affected {} rows\", rowsAffected)", operation);
        }
        
        codeBuilder.addStatement("return rowsAffected");
        
        return codeBuilder.build();
    }
    
    // =========================== UTILITY METHODS ===========================
    
    /**
     * Formats SQL using SqlFormatter with consistent settings.
     */
    public static String formatSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }
        
        String formatted = SqlFormatter.format(sql);
        // Fix colon spacing for named parameters
        return formatted.replace(": ", ":");
    }
    
    /**
     * Generates meaningful parameter names for WHERE clause parameters.
     */
    private static String generateWhereParamName(ColumnMetadata column, int index) {
        String columnName = column.getColumnName();
        
        // If column name is meaningful, use it
        if (!columnName.startsWith("whereParam")) {
            return NamingConventions.parameterName(columnName);
        }
        
        // Generate names based on common patterns
        return switch (index) {
            case 0 -> "id";
            case 1 -> "status";
            case 2 -> "category";
            default -> "param" + (index + 1);
        };
    }
    
    /**
     * Infers actual column name for WHERE clauses (handles parameterized cases).
     */
    private static String inferColumnName(ColumnMetadata column, int index) {
        String columnName = column.getColumnName();
        
        // If we have a real column name, use it
        if (!columnName.startsWith("whereParam")) {
            return columnName;
        }
        
        // For parameterized queries, guess common column names
        return switch (index) {
            case 0 -> "id";
            case 1 -> "status";
            case 2 -> "category";
            default -> "column" + (index + 1);
        };
    }
    
    // =========================== VALIDATION HELPERS ===========================
    
    private static void validateNotEmpty(String value, String parameterName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be null or empty");
        }
    }
    
    private static void validateNotEmpty(List<?> list, String parameterName) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be null or empty");
        }
    }
}