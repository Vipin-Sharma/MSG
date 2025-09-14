package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.jfeatures.msg.codegen.domain.DBColumn;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParameterMetadataExtractor {

    private static final int MAX_SQL_LENGTH = 10_000;
    private static final Pattern COLUMN_PATTERN = Pattern.compile("(\\w+\\.\\w+|\\w+)\\s*=\\s*\\?", Pattern.CASE_INSENSITIVE);
    private static final Pattern WHERE_PATTERN = Pattern.compile("\\bwhere\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private final DataSource dataSource;
    
    public ParameterMetadataExtractor(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException(ProjectConstants.ERROR_NULL_DATASOURCE);
        }
        this.dataSource = dataSource;
    }
    
    public List<DBColumn> extractParameters(String sql) throws SQLException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException(ProjectConstants.ERROR_NULL_SQL);
        }
        if (sql.length() > MAX_SQL_LENGTH) {
            throw new IllegalArgumentException("SQL query too long");
        }
        
        List<DBColumn> parameters = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ParameterMetaData pmd = ps.getParameterMetaData();
            int parameterCount = pmd.getParameterCount();
            
            log.info("Found {} parameters in SQL query", parameterCount);
            
            // Extract column names from WHERE clause
            List<String> columnNames = extractColumnNamesFromWhereClause(sql, parameterCount);
            log.info("Extracted column names: {}", columnNames);
            
            for (int i = 1; i <= parameterCount; i++) {
                try {
                    int sqlType = pmd.getParameterType(i);
                    String parameterName = (i <= columnNames.size()) ? 
                        columnNames.get(i - 1) : "param" + i;
                    String javaType = getJavaTypeForSqlType(sqlType);
                    String jdbcType = getJdbcTypeForSqlType(sqlType);
                    
                    DBColumn parameter = new DBColumn(null, parameterName, javaType, jdbcType);
                    parameters.add(parameter);
                    
                    log.info("Parameter {}: Name={}, SQL Type={}, Java Type={}, JDBC Type={}", 
                            i, parameterName, sqlType, javaType, jdbcType);
                    
                } catch (SQLException e) {
                    log.warn("Could not get metadata for parameter {}: {}", i, e.getMessage());
                    String parameterName = (i <= columnNames.size()) ? 
                        columnNames.get(i - 1) : "param" + i;
                    DBColumn defaultParam = new DBColumn(null, parameterName, "String", "VARCHAR");
                    parameters.add(defaultParam);
                }
            }
        }
        
        return parameters;
    }
    
    private List<String> extractColumnNamesFromWhereClause(String sql, int expectedParameterCount) {
        List<String> columnNames = new ArrayList<>();
        
        try {
            // Extract WHERE clause from SQL
            String whereClause = extractWhereClause(sql);
            if (whereClause == null || whereClause.isEmpty()) {
                log.warn("Could not extract WHERE clause from SQL, using default parameter names");
                return generateDefaultParameterNames(expectedParameterCount);
            }

            if (whereClause.length() > MAX_SQL_LENGTH) {
                throw new IllegalArgumentException("WHERE clause too long");
            }

            // Find all column references before '?' parameters
            Matcher matcher = COLUMN_PATTERN.matcher(whereClause);
            
            while (matcher.find() && columnNames.size() < expectedParameterCount) {
                String columnReference = matcher.group(1);
                String parameterName = convertToParameterName(columnReference);
                columnNames.add(parameterName);
                log.info("Extracted parameter name: {} from column: {}", parameterName, columnReference);
            }
            
            // Fill remaining parameters with default names if needed
            while (columnNames.size() < expectedParameterCount) {
                columnNames.add("param" + (columnNames.size() + 1));
            }
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Invalid SQL structure while extracting column names: {}", e.getMessage());
            return generateDefaultParameterNames(expectedParameterCount);
        } catch (RuntimeException e) {
            log.error("Unexpected runtime error extracting column names from WHERE clause: {}", e.getMessage(), e);
            return generateDefaultParameterNames(expectedParameterCount);
        }
        
        return columnNames;
    }
    
    private String extractWhereClause(String sql) {
        if (sql.length() > MAX_SQL_LENGTH) {
            throw new IllegalArgumentException("SQL query too long");
        }

        // Remove line breaks and extra spaces for easier parsing
        String normalizedSql = WHITESPACE_PATTERN.matcher(sql).replaceAll(" ").trim();

        // Find WHERE keyword (case insensitive)
        Matcher whereMatcher = WHERE_PATTERN.matcher(normalizedSql);
        
        if (!whereMatcher.find()) {
            return null;
        }
        
        int whereStart = whereMatcher.start();
        
        // Find potential end keywords (case insensitive)
        String[] endKeywords = {"group by", "order by", "having", "limit", "offset"};
        int whereEnd = normalizedSql.length();
        
        for (String keyword : endKeywords) {
            Pattern endPattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher endMatcher = endPattern.matcher(normalizedSql);
            if (endMatcher.find(whereStart)) {
                whereEnd = Math.min(whereEnd, endMatcher.start());
            }
        }
        
        return normalizedSql.substring(whereStart + 5, whereEnd).trim(); // +5 for "WHERE"
    }
    
    private String convertToParameterName(String columnReference) {
        // Remove table alias prefix (e.g., "cus.customer_id" -> "customer_id")
        String columnName = columnReference.contains(".") ? 
            columnReference.substring(columnReference.lastIndexOf(".") + 1) : columnReference;
        
        // Convert snake_case to camelCase
        return toCamelCase(columnName);
    }
    
    private String toCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        
        StringBuilder camelCase = new StringBuilder();
        boolean capitalizeNext = false;
        
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                camelCase.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                camelCase.append(Character.toLowerCase(c));
            }
        }
        
        return camelCase.toString();
    }
    
    private List<String> generateDefaultParameterNames(int count) {
        List<String> defaultNames = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            defaultNames.add("param" + i);
        }
        return defaultNames;
    }
    
    private String getJavaTypeForSqlType(int sqlType) {
        return switch (sqlType) {
            case Types.INTEGER, Types.SMALLINT, Types.TINYINT -> "Integer";
            case Types.BIGINT -> "Long";
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR, Types.NVARCHAR, Types.NCHAR -> "String";
            case Types.DECIMAL, Types.NUMERIC -> "BigDecimal";
            case Types.DOUBLE, Types.FLOAT -> "Double";
            case Types.REAL -> "Float";
            case Types.BOOLEAN, Types.BIT -> "Boolean";
            case Types.DATE -> "Date";
            case Types.TIME -> "Time";
            case Types.TIMESTAMP -> "Timestamp";
            case Types.BLOB -> "byte[]";
            default -> "String"; // Default fallback
        };
    }
    
    private String getJdbcTypeForSqlType(int sqlType) {
        return switch (sqlType) {
            case Types.INTEGER -> "INTEGER";
            case Types.SMALLINT -> "SMALLINT";
            case Types.TINYINT -> "TINYINT";
            case Types.BIGINT -> "BIGINT";
            case Types.VARCHAR -> "VARCHAR";
            case Types.CHAR -> "CHAR";
            case Types.LONGVARCHAR -> "LONGVARCHAR";
            case Types.NVARCHAR -> "NVARCHAR";
            case Types.NCHAR -> "NCHAR";
            case Types.DECIMAL -> "DECIMAL";
            case Types.NUMERIC -> "NUMERIC";
            case Types.DOUBLE -> "DOUBLE";
            case Types.FLOAT -> "FLOAT";
            case Types.REAL -> "REAL";
            case Types.BOOLEAN -> "BOOLEAN";
            case Types.BIT -> "BIT";
            case Types.DATE -> "DATE";
            case Types.TIME -> "TIME";
            case Types.TIMESTAMP -> "TIMESTAMP";
            case Types.BLOB -> "BLOB";
            default -> "VARCHAR"; // Default fallback
        };
    }
}