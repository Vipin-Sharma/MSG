package com.jfeatures.msg.codegen.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public final class JavaPoetClassNameBuilder {

    private JavaPoetClassNameBuilder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    public static TypeName buildJavaPoetTypeName(String targetJavaPackageName, String sqlBusinessDomainName, String classType) {
        if (targetJavaPackageName == null || targetJavaPackageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Target Java package name cannot be null or empty");
        }
        if (sqlBusinessDomainName == null || sqlBusinessDomainName.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL business domain name cannot be null or empty");
        }
        if (classType == null || classType.trim().isEmpty()) {
            throw new IllegalArgumentException("Class type cannot be null or empty");
        }
        
        return ClassName.get(targetJavaPackageName, sqlBusinessDomainName + classType);
    }
}