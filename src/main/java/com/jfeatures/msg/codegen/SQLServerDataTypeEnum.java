package com.jfeatures.msg.codegen;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enumeration of SQL Server data types and their corresponding Java types.
 * Maps SQL Server database types to appropriate Java classes and JDBC types.
 * 
 * <p>Based on Microsoft documentation: 
 * <a href="https://docs.microsoft.com/en-us/sql/connect/jdbc/using-basic-data-types?view=sql-server-ver16">
 * Using Basic Data Types</a></p>
 * 
 * @author MSG Development Team
 * @version 1.0
 */
public enum SQLServerDataTypeEnum {

    BIGINT("BIGINT", Long.class, JdbcType.LONG),
    BINARY("BINARY", byte[].class, JdbcType.BYTE_ARRAY),
    BIT("BIT", Boolean.class, JdbcType.BOOLEAN),
    CHAR("CHAR", String.class, JdbcType.STRING),
    DATE("DATE", java.sql.Date.class, JdbcType.DATE),
    DATETIME("DATETIME", java.sql.Timestamp.class, JdbcType.TIMESTAMP),
    DATETIME2("DATETIME2", java.sql.Timestamp.class, JdbcType.TIMESTAMP),
    DECIMAL("DECIMAL", java.math.BigDecimal.class, JdbcType.BIG_DECIMAL),
    INT("INT", Integer.class, JdbcType.INT),
    FLOAT("FLOAT", Double.class, JdbcType.DOUBLE),
    IMAGE("IMAGE", byte[].class, JdbcType.BYTE_ARRAY),
    MONEY("MONEY", java.math.BigDecimal.class, JdbcType.BIG_DECIMAL),
    NCHAR("NCHAR", String.class, JdbcType.STRING),
    NTEXT("NTEXT", String.class, JdbcType.STRING),
    NUMERIC("NUMERIC", java.math.BigDecimal.class, JdbcType.BIG_DECIMAL),
    NVARCHAR("NVARCHAR", String.class, JdbcType.STRING),
    REAL("REAL", Float.class, JdbcType.FLOAT),
    SMALLDATETIME("SMALLDATETIME", java.sql.Timestamp.class, JdbcType.TIMESTAMP),
    SMALLINT("SMALLINT", Integer.class, JdbcType.INT),
    SMALLMONEY("SMALLMONEY", java.math.BigDecimal.class, JdbcType.BIG_DECIMAL),
    TEXT("TEXT", String.class, JdbcType.STRING),
    TIME("TIME", java.sql.Time.class, JdbcType.TIME),
    TIMESTAMP("TIMESTAMP", java.sql.Timestamp.class, JdbcType.TIMESTAMP),
    TINYINT("TINYINT", Integer.class, JdbcType.INT),
    VARCHAR("VARCHAR", String.class, JdbcType.STRING),

    // Additional SQL Server types for completeness
    UNIQUEIDENTIFIER("UNIQUEIDENTIFIER", String.class, JdbcType.STRING),
    VARBINARY("VARBINARY", byte[].class, JdbcType.BYTE_ARRAY),
    XML("XML", String.class, JdbcType.STRING);

    /**
     * JDBC type constants to avoid string duplication
     */
    private static final class JdbcType {
        static final String STRING = "String";
        static final String BYTE_ARRAY = "Byte[]";
        static final String TIMESTAMP = "Timestamp";
        static final String BIG_DECIMAL = "BigDecimal";
        static final String BOOLEAN = "Boolean";
        static final String DATE = "Date";
        static final String TIME = "Time";
        static final String INT = "Int";
        static final String LONG = "Long";
        static final String DOUBLE = "Double";
        static final String FLOAT = "Float";

        private JdbcType() {}
    }

    // Constant for default JDBC type (matches VARCHAR)
    private static final String JDBC_DEFAULT_TYPE = "VARCHAR";

    private final String dbDataType;
    private final Class<?> javaClassType;
    private final String jdbcType;
    
    // Performance optimization: Cache lookups in static maps
    private static final Map<String, SQLServerDataTypeEnum> DB_TYPE_LOOKUP;
    
    static {
        DB_TYPE_LOOKUP = Arrays.stream(values())
                .collect(Collectors.toMap(
                    e -> e.dbDataType.toUpperCase(), 
                    e -> e
                ));
    }


    SQLServerDataTypeEnum(String dbDataType, Class<?> cl, String jdbcType)
    {
        this.dbDataType = dbDataType;
        this.javaClassType = cl;
        this.jdbcType = jdbcType;
    }

    /**
     * Retrieves the Java class type for a given SQL Server data type.
     * 
     * @param type The SQL Server data type (case-insensitive)
     * @return The corresponding Java class, or String.class if type not found
     * @throws IllegalArgumentException if the type is null or empty
     */
    public static Class<?> getClassForType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Database type cannot be null or empty");
        }
        
        SQLServerDataTypeEnum enumValue = DB_TYPE_LOOKUP.get(type.toUpperCase().trim());
        return enumValue != null ? enumValue.getJavaClassType() : String.class;
    }

    public java.lang.String getDbDataType() {
        return dbDataType;
    }

    public Class<?> getJavaClassType() {
        return javaClassType;
    }

    /**
     * Retrieves the JDBC type string for a given SQL Server data type.
     * 
     * @param dbDataType The SQL Server data type (case-insensitive)
     * @return The corresponding JDBC type string, or the default JDBC type if none is found
     * @throws IllegalArgumentException if the dbDataType is null or empty
     */
    public static String getJdbcTypeForDBType(String dbDataType) {
        if (dbDataType == null || dbDataType.trim().isEmpty()) {
            throw new IllegalArgumentException("Database type cannot be null or empty");
        }
        
        SQLServerDataTypeEnum enumValue = DB_TYPE_LOOKUP.get(dbDataType.toUpperCase().trim());
        return enumValue != null ? enumValue.jdbcType : JDBC_DEFAULT_TYPE;
    }

}
