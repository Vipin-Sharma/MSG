package com.jfeatures.msg.codegen.dbmetadata;

import lombok.Data;

@Data
public class ColumnMetadata {
    private String columnName;
    private String columnAlias;
    private String tableName;
    private int columnType;
    private String columnTypeName;
    private String columnClassName;
    private int columnDisplaySize;
    private int precision;
    private int scale;
    private int isNullable;
    private boolean isAutoIncrement;
    private boolean isCaseSensitive;
    private boolean isReadOnly;
    private boolean isWritable;
    private boolean isDefinitelyWritable;
    private boolean isCurrency;
    private boolean isSigned;
}
