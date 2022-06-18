package com.jfeatures.msg.codegen.domain;

public record DBColumn(String tableName, String columnName, String javaType, String jdbcType) { }
