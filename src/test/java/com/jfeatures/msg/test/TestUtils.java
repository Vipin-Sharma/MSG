package com.jfeatures.msg.test;

import com.jfeatures.msg.codegen.SQLServerDataTypeEnum;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;

import java.sql.Types;

/**
 * Utility class for test data creation
 */
public class TestUtils {
    
    /**
     * Creates a ColumnMetadata object with proper initialization for testing
     */
    public static ColumnMetadata createColumnMetadata(String columnName, String columnTypeName, int columnType, boolean isNullable) {
        ColumnMetadata metadata = new ColumnMetadata();
        metadata.setColumnName(columnName);
        metadata.setColumnTypeName(columnTypeName);
        metadata.setColumnType(columnType);
        metadata.setIsNullable(isNullable ? 1 : 0);
        metadata.setColumnClassName(getJavaTypeForSqlType(columnType));
        return metadata;
    }
    
    /**
     * Maps SQL types to Java class names for test data
     */
    private static String getJavaTypeForSqlType(int sqlType) {
        return switch (sqlType) {
            case Types.INTEGER -> "java.lang.Integer";
            case Types.BIGINT -> "java.lang.Long";
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR -> "java.lang.String";
            case Types.DECIMAL, Types.NUMERIC -> "java.math.BigDecimal";
            case Types.DOUBLE, Types.FLOAT -> "java.lang.Double";
            case Types.BOOLEAN, Types.BIT -> "java.lang.Boolean";
            case Types.DATE -> "java.sql.Date";
            case Types.TIME -> "java.sql.Time";
            case Types.TIMESTAMP -> "java.sql.Timestamp";
            case Types.BINARY, Types.VARBINARY -> "byte[]";
            case Types.SMALLINT -> "java.lang.Short";
            case Types.TINYINT -> "java.lang.Byte";
            default -> "java.lang.Object";
        };
    }
    
    /**
     * Create ColumnMetadata with alias for testing
     */
    public static ColumnMetadata createColumnMetadataWithAlias(String columnName, String columnAlias, String columnTypeName, int columnType, boolean isNullable) {
        ColumnMetadata metadata = createColumnMetadata(columnName, columnTypeName, columnType, isNullable);
        metadata.setColumnAlias(columnAlias);
        return metadata;
    }
}