package com.jfeatures.msg.codegen.util;

public final class JavaPackageNameBuilder {

    private JavaPackageNameBuilder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    public static String buildJavaPackageName(String sqlBusinessDomainName, String packageType) {
        if (sqlBusinessDomainName == null || sqlBusinessDomainName.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL business domain name cannot be null or empty");
        }
        if (packageType == null || packageType.trim().isEmpty()) {
            throw new IllegalArgumentException("Package type cannot be null or empty");
        }
        
        return "com.jfeatures.msg." + sqlBusinessDomainName.toLowerCase() + "." + packageType;
    }
}