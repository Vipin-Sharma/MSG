package com.jfeatures.msg.codegen;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum SQLServerDataTypeEnum {

    //todo utilize int rather than Integer
    Integer("INT", Integer.class),
    String("NVARCHAR", String.class);
    //todo add more types on basis of all possible type for SQL server, use a ddl which has all types that can help to print all such values.

    private final String dataType;
    private final Class aClass;


    SQLServerDataTypeEnum(String dataType, Class cl)
    {
        this.dataType = dataType;
        this.aClass = cl;
    }

    public static Class getClassForType(String type)
    {
        List<SQLServerDataTypeEnum> TypeList = Arrays.stream(SQLServerDataTypeEnum.values())
                .filter(SQLServerDataTypeEnum -> Objects.equals(SQLServerDataTypeEnum.getDataType(), type))
                .collect(Collectors.toList());
        return TypeList.get(0).getaClass();
    }

    public java.lang.String getDataType() {
        return dataType;
    }

    public Class getaClass() {
        return aClass;
    }
}
