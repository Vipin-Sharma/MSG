package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import org.apache.commons.text.CaseUtils;

/**
 * Centralized naming convention utilities for code generation.
 * Follows Clean Code principles: single responsibility for naming logic.
 */
public final class NamingConventions {
    
    // Private constructor to prevent instantiation
    private NamingConventions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // =========================== PACKAGE NAMES ===========================
    
    /**
     * Creates a standard package name for generated classes.
     * Pattern: com.jfeatures.msg.{businessName}.{packageSuffix}
     */
    public static String buildPackageName(String businessName, String packageSuffix) {
        validateNotEmpty(businessName, "businessName");
        validateNotEmpty(packageSuffix, "packageSuffix");
        
        return "com.jfeatures.msg." + businessName.toLowerCase() + "." + packageSuffix;
    }
    
    // =========================== CLASS NAMES ===========================
    
    /**
     * Creates a standard class name for generated classes.
     * Pattern: {BusinessName}{ClassSuffix}
     */
    public static String buildClassName(String businessName, String classSuffix) {
        validateNotEmpty(businessName, "businessName");
        validateNotEmpty(classSuffix, "classSuffix");
        
        // Handle empty string cases
        if (businessName.isEmpty()) {
            return classSuffix;
        }
        
        // If businessName is already properly capitalized, use it as-is
        // Otherwise, capitalize first letter
        String capitalizedBusinessName = Character.isUpperCase(businessName.charAt(0)) && 
                                        !businessName.contains("_") && !businessName.contains("-") ?
                                        businessName :
                                        CaseUtils.toCamelCase(businessName, true);
        return capitalizedBusinessName + classSuffix;
    }
    
    /**
     * Creates DAO class name.
     */
    public static String daoClassName(String businessName) {
        return buildClassName(businessName, "DAO");
    }
    
    /**
     * Creates Controller class name.
     */
    public static String controllerClassName(String businessName) {
        return buildClassName(businessName, "Controller");
    }
    
    /**
     * Creates DTO class name.
     */
    public static String dtoClassName(String businessName) {
        return buildClassName(businessName, "DTO");
    }
    
    // =========================== FIELD NAMES ===========================
    
    /**
     * Creates a field name for JDBC template following naming conventions.
     */
    public static String jdbcTemplateFieldName(String businessName) {
        validateNotEmpty(businessName, "businessName");
        return CaseUtils.toCamelCase(businessName, false) + "NamedParameterJdbcTemplate";
    }
    
    /**
     * Creates a DAO field name for controllers.
     */
    public static String daoFieldName(String businessName) {
        validateNotEmpty(businessName, "businessName");
        return CaseUtils.toCamelCase(businessName, false) + "DAO";
    }
    
    /**
     * Creates a SQL constant field name.
     */
    public static String sqlFieldName(String operation) {
        return operation != null && !operation.isEmpty() ? 
                operation.toUpperCase() + "_SQL" : CodeGenerationConstants.SQL_FIELD_NAME;
    }
    
    // =========================== METHOD NAMES ===========================
    
    /**
     * Creates getter method name from field name.
     */
    public static String getterMethodName(String fieldName) {
        validateNotEmpty(fieldName, "fieldName");
        return "get" + CaseUtils.toCamelCase(fieldName, true, '_');
    }
    
    /**
     * Creates setter method name from field name.
     */
    public static String setterMethodName(String fieldName) {
        validateNotEmpty(fieldName, "fieldName");
        return "set" + CaseUtils.toCamelCase(fieldName, true, '_');
    }
    
    /**
     * Creates DAO method name.
     */
    public static String daoMethodName(String operation, String businessName) {
        validateNotEmpty(operation, "operation");
        validateNotEmpty(businessName, "businessName");
        return operation.toLowerCase() + businessName;
    }
    
    /**
     * Creates controller endpoint method name.
     */
    public static String controllerMethodName(String operation, String businessName) {
        validateNotEmpty(operation, "operation");
        validateNotEmpty(businessName, "businessName");
        return operation.toLowerCase() + "DataFor" + businessName;
    }
    
    // =========================== PARAMETER NAMES ===========================
    
    /**
     * Converts column name to parameter name (camelCase).
     */
    public static String parameterName(String columnName) {
        validateNotEmpty(columnName, "columnName");
        return CaseUtils.toCamelCase(columnName, false, '_');
    }
    
    /**
     * Converts column name to field name (camelCase).
     */
    public static String fieldName(String columnName) {
        return parameterName(columnName);
    }
    
    // =========================== VALIDATION ===========================
    
    private static void validateNotEmpty(String value, String parameterName) {
        if (value == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }
}