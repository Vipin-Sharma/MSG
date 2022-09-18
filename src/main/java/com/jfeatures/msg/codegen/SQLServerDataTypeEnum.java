package com.jfeatures.msg.codegen;

import java.util.Arrays;
import java.util.List;

/**
 * Source of these types is: <a href="https://docs.microsoft.com/en-us/sql/connect/jdbc/using-basic-data-types?view=sql-server-ver16">...</a>
 */
public enum SQLServerDataTypeEnum {

    //todo Fix commented types
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
    VARCHAR("VARCHAR", String.class, "String" );
    /*(varchar(max)    String );
    (xml, String );
    (sqlvariant  Object );
    (geometry, byte[] );
    (geography, byte[] );*/


    private final String dbDataType;
    private final Class<?> javaClassType;
    private final String jdbcType;


    SQLServerDataTypeEnum(String dbDataType, Class<?> cl, String jdbcType)
    {
        this.dbDataType = dbDataType;
        this.javaClassType = cl;
        this.jdbcType = jdbcType;
    }

    public static Class<?> getClassForType(String type)
    {
        List<SQLServerDataTypeEnum> TypeList = Arrays.stream(values())
                .filter(SQLServerDataTypeEnum -> SQLServerDataTypeEnum.getDbDataType().equalsIgnoreCase( type))
                .toList();
        return TypeList.get(0).getJavaClassType();
    }

    public java.lang.String getDbDataType() {
        return dbDataType;
    }

    public Class<?> getJavaClassType() {
        return javaClassType;
    }

    public static String getJdbcTypeForDBType(String DbDataType)
    {
        List<SQLServerDataTypeEnum> TypeList = Arrays.stream(values())
                .filter(SQLServerDataTypeEnum -> SQLServerDataTypeEnum.getDbDataType().equalsIgnoreCase( DbDataType))
                .toList();
        return TypeList.get(0).jdbcType;
    }
}
