package com.jfeatures.msg.test;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import java.sql.Types;

/**
 * Utility class for creating database metadata objects used in tests.
 */
public final class TestUtils {

    private TestUtils() {
        // prevents instantiation
    }

    /**
     * Creates {@link ColumnMetadata} fully initialized for testing scenarios.
     *
     * @param columnName the database column name
     * @param columnTypeName the database column type name
     * @param columnType the {@link java.sql.Types} value representing the column type
     * @param isNullable whether the column allows null values
     * @return fully initialized {@link ColumnMetadata} instance for tests
     */
    public static ColumnMetadata createColumnMetadata(
        String columnName,
        String columnTypeName,
        int columnType,
        boolean isNullable
    ) {
        ColumnMetadata metadata = new ColumnMetadata();
        metadata.setColumnName(columnName);
        metadata.setColumnTypeName(columnTypeName);
        metadata.setColumnType(columnType);
        metadata.setIsNullable(isNullable ? 1 : 0);
        metadata.setColumnClassName(getJavaTypeForSqlType(columnType));
        return metadata;
    }

    /**
     * Maps {@link java.sql.Types} values to their corresponding Java class names used by tests.
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
}
