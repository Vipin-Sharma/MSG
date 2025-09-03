package com.jfeatures.msg.codegen.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public class JavaPoetTypeNameBuilder {
    
    public static ParameterizedTypeName buildParameterizedTypeName(TypeName dtoTypeName, ClassName containerType) {
        if (dtoTypeName == null) {
            throw new IllegalArgumentException("DTO type name cannot be null");
        }
        if (containerType == null) {
            throw new IllegalArgumentException("Container type cannot be null");
        }
        
        return ParameterizedTypeName.get(containerType, dtoTypeName);
    }

    public static TypeName buildJavaPoetTypeNameForClass(String sqlBusinessDomainName, String packageType, String classType) {
        if (sqlBusinessDomainName == null || sqlBusinessDomainName.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL business domain name cannot be null or empty");
        }
        if (packageType == null || packageType.trim().isEmpty()) {
            throw new IllegalArgumentException("Package type cannot be null or empty");
        }
        if (classType == null || classType.trim().isEmpty()) {
            throw new IllegalArgumentException("Class type cannot be null or empty");
        }
        
        return JavaPoetClassNameBuilder.buildJavaPoetTypeName(
            JavaPackageNameBuilder.buildJavaPackageName(sqlBusinessDomainName, packageType), 
            sqlBusinessDomainName, 
            classType);
    }
}