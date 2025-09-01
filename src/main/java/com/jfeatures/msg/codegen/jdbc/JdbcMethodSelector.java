package com.jfeatures.msg.codegen.jdbc;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;

public class JdbcMethodSelector {
    
    public static String selectJdbcGetterMethodFor(ColumnMetadata databaseColumnDefinition) {
        if (databaseColumnDefinition == null) {
            throw new IllegalArgumentException("Database column definition cannot be null");
        }
        if (databaseColumnDefinition.getColumnTypeName() == null || databaseColumnDefinition.getColumnTypeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Database column type name cannot be null or empty");
        }
        
        String columnTypeName = databaseColumnDefinition.getColumnTypeName().toLowerCase();
        return switch (columnTypeName) {
            case "varchar", "char", "nvarchar", "nchar", "text", "ntext" -> "getString";
            case "int", "integer" -> "getInt";
            case "bigint" -> "getLong";
            case "smallint", "tinyint" -> "getShort";
            case "decimal", "numeric", "money", "smallmoney" -> "getBigDecimal";
            case "float", "real" -> "getFloat";
            case "double" -> "getDouble";
            case "bit" -> "getBoolean";
            case "datetime", "datetime2", "smalldatetime" -> "getTimestamp";
            case "date" -> "getDate";
            case "time" -> "getTime";
            case "binary", "varbinary", "image" -> "getBytes";
            default -> {
                // Log the unknown type for debugging
                System.err.println("Warning: Unknown column type '" + columnTypeName + "' for column '" + 
                    databaseColumnDefinition.getColumnName() + "', defaulting to getString");
                yield "getString";
            }
        };
    }
}