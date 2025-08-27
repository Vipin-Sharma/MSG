package com.jfeatures.msg.codegen.util;

public class JavaPackageNameBuilder {
    
    public static String buildJavaPackageName(String sqlBusinessDomainName, String packageType) {
        if (sqlBusinessDomainName == null || sqlBusinessDomainName.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL business domain name cannot be null or empty");
        }
        if (packageType == null || packageType.trim().isEmpty()) {
            throw new IllegalArgumentException("Package type cannot be null or empty");
        }
        
        return "com.jfeatures.msg." + sqlBusinessDomainName + "." + packageType;
    }
}