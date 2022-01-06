package com.jfeatures.msg.codegen;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum TypeEnum {

    Integer("INT", Integer.class),
    String("NVARCHAR", String.class);
    //todo add more types on basis of all possible type for SQL server, use a ddl which has all types that can help to print all such values.

    private final String dataType;
    private final Class cl;


    TypeEnum(String dataType, Class cl)
    {
        this.dataType = dataType;
        this.cl = cl;
    }

    public static Class getClassForType(String type)
    {
        List<TypeEnum> TypeList = Arrays.stream(TypeEnum.values())
                .filter(typeEnum -> Objects.equals(typeEnum.getDataType(), type))
                .collect(Collectors.toList());
        return TypeList.get(0).getCl();
    }

    public java.lang.String getDataType() {
        return dataType;
    }

    public Class getCl() {
        return cl;
    }
}
