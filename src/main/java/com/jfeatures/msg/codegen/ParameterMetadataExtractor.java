package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.DBColumn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ParameterMetadataExtractor {
    
    private final DataSource dataSource;
    
    public ParameterMetadataExtractor(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public List<DBColumn> extractParameters(String sql) throws SQLException {
        List<DBColumn> parameters = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ParameterMetaData pmd = ps.getParameterMetaData();
            int parameterCount = pmd.getParameterCount();
            
            log.info("Found {} parameters in SQL query", parameterCount);
            
            for (int i = 1; i <= parameterCount; i++) {
                try {
                    int sqlType = pmd.getParameterType(i);
                    String parameterName = "param" + i; // Default parameter name
                    String javaType = getJavaTypeForSqlType(sqlType);
                    String jdbcType = getJdbcTypeForSqlType(sqlType);
                    
                    DBColumn parameter = new DBColumn(null, parameterName, javaType, jdbcType);
                    parameters.add(parameter);
                    
                    log.info("Parameter {}: SQL Type={}, Java Type={}, JDBC Type={}", 
                            i, sqlType, javaType, jdbcType);
                    
                } catch (SQLException e) {
                    log.warn("Could not get metadata for parameter {}: {}", i, e.getMessage());
                    // Create default parameter if metadata extraction fails
                    DBColumn defaultParam = new DBColumn(null, "param" + i, "String", "VARCHAR");
                    parameters.add(defaultParam);
                }
            }
        }
        
        return parameters;
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