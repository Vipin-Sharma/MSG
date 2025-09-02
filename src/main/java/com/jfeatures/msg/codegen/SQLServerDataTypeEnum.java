package com.jfeatures.msg.codegen;

import java.util.Arrays;
import java.util.List;
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

    BIGINT("BIGINT", Long.class, "Long" ),
    BINARY("BINARY", byte[].class, "Byte[]" ),
    BIT("BIT", Boolean.class, "Boolean"),
    CHAR("CHAR", String.class, "String" ),
    DATE("DATE", java.sql.Date.class, "Date"),
    DATETIME("DATETIME", java.sql.Timestamp.class, "Timestamp" ),
    DATETIME2("DATETIME2", java.sql.Timestamp.class, "Timestamp" ),
    //DATETIMEOFFSET("DATETIMEOFFSET", microsoft.sql.DateTimeOffset.class, "microsoft.sql.Types.DATETIMEOFFSET" ),
    DECIMAL("DECIMAL", java.math.BigDecimal.class, "BigDecimal" ),
    INT("INT", Integer.class, "Int" ),
    FLOAT("FLOAT", Double.class, "Double" ),
    IMAGE("IMAGE", byte[].class, "Byte[]" ),
    MONEY("MONEY", java.math.BigDecimal.class, "BigDecimal" ),
    NCHAR("NCHAR", String.class, "String" ),
    NTEXT("NTEXT", String.class, "String" ),
    NUMERIC("NUMERIC", java.math.BigDecimal.class, "BigDecimal" ),
    NVARCHAR("NVARCHAR", String.class, "String"),
    /*(nvarchar(max)   String );*/
    REAL("REAL", Float.class, "Float" ),
    SMALLDATETIME("SMALLDATETIME", java.sql.Timestamp.class, "Timestamp" ),
    SMALLINT("SMALLINT", Integer.class, "Int" ),
    SMALLMONEY("SMALLMONEY", java.math.BigDecimal.class, "BigDecimal" ),
    TEXT("TEXT", String.class, "String" ),
    TIME("TIME", java.sql.Time.class, "Time" ),
    TIMESTAMP("TIMESTAMP", java.sql.Timestamp.class, "Timestamp" ),
    TINYINT("TINYINT", Integer.class, "Int" ),
    /*(udt, byte[] ),
    (uniqueidentifier, String );
    (varbinary, byte[] );
    (varbinar(max)   byte[] );*/
    VARCHAR("VARCHAR", String.class, "String" ),
    
    // Additional SQL Server types for completeness
    UNIQUEIDENTIFIER("UNIQUEIDENTIFIER", String.class, "String"),
    VARBINARY("VARBINARY", byte[].class, "Byte[]"),
    XML("XML", String.class, "String");

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
     * @return The corresponding JDBC type string, or "VARCHAR" if type not found
     * @throws IllegalArgumentException if the dbDataType is null or empty
     */
    public static String getJdbcTypeForDBType(String dbDataType) {
        if (dbDataType == null || dbDataType.trim().isEmpty()) {
            throw new IllegalArgumentException("Database type cannot be null or empty");
        }
        
        SQLServerDataTypeEnum enumValue = DB_TYPE_LOOKUP.get(dbDataType.toUpperCase().trim());
        return enumValue != null ? enumValue.jdbcType : "VARCHAR";
    }
}
