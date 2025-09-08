package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.SQLServerDataTypeEnum;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Specialized builder for JavaPoet parameter specifications.
 * Follows Single Responsibility Principle - only parameter creation logic.
 */
public final class ParameterBuilders {
    
    private ParameterBuilders() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // =========================== REQUEST PARAMETER BUILDERS ===========================
    
    /**
     * Creates a @RequestParam parameter with default settings.
     */
    public static ParameterSpec requestParam(TypeName type, String paramName) {
        validateNotNull(type, "type");
        validateNotEmpty(paramName, "paramName");
        
        return ParameterSpec.builder(type, paramName)
                .addAnnotation(RequestParam.class)
                .build();
    }
    
    /**
     * Creates a @RequestParam parameter with custom request parameter name.
     */
    public static ParameterSpec requestParamWithName(TypeName type, String paramName, String requestParamName) {
        validateNotNull(type, "type");
        validateNotEmpty(paramName, "paramName");
        validateNotEmpty(requestParamName, "requestParamName");
        
        return ParameterSpec.builder(type, paramName)
                .addAnnotation(AnnotationSpec.builder(RequestParam.class)
                        .addMember("value", "$S", requestParamName)
                        .build())
                .build();
    }
    
    /**
     * Creates an optional @RequestParam parameter.
     */
    public static ParameterSpec optionalRequestParam(TypeName type, String paramName, Object defaultValue) {
        validateNotNull(type, "type");
        validateNotEmpty(paramName, "paramName");
        
        AnnotationSpec.Builder requestParamBuilder = AnnotationSpec.builder(RequestParam.class)
                .addMember("required", "$L", false);
        
        if (defaultValue != null) {
            if (defaultValue instanceof String) {
                requestParamBuilder.addMember("defaultValue", "$S", defaultValue);
            } else {
                requestParamBuilder.addMember("defaultValue", "$L", defaultValue);
            }
        }
        
        return ParameterSpec.builder(type, paramName)
                .addAnnotation(requestParamBuilder.build())
                .build();
    }
    
    // =========================== PATH VARIABLE BUILDERS ===========================
    
    /**
     * Creates a @PathVariable parameter.
     */
    public static ParameterSpec pathVariable(TypeName type, String paramName) {
        validateNotNull(type, "type");
        validateNotEmpty(paramName, "paramName");
        
        return ParameterSpec.builder(type, paramName)
                .addAnnotation(PathVariable.class)
                .build();
    }
    
    /**
     * Creates a @PathVariable parameter with custom path variable name.
     */
    public static ParameterSpec pathVariableWithName(TypeName type, String paramName, String pathVarName) {
        validateNotNull(type, "type");
        validateNotEmpty(paramName, "paramName");
        validateNotEmpty(pathVarName, "pathVarName");
        
        return ParameterSpec.builder(type, paramName)
                .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                        .addMember("value", "$S", pathVarName)
                        .build())
                .build();
    }
    
    // =========================== REQUEST BODY BUILDERS ===========================
    
    /**
     * Creates a @Valid @RequestBody parameter for DTO objects.
     */
    public static ParameterSpec validRequestBody(TypeName type, String paramName) {
        validateNotNull(type, "type");
        validateNotEmpty(paramName, "paramName");
        
        return ParameterSpec.builder(type, paramName)
                .addAnnotation(Valid.class)
                .addAnnotation(RequestBody.class)
                .build();
    }
    
    /**
     * Creates a simple @RequestBody parameter without validation.
     */
    public static ParameterSpec requestBody(TypeName type, String paramName) {
        validateNotNull(type, "type");
        validateNotEmpty(paramName, "paramName");
        
        return ParameterSpec.builder(type, paramName)
                .addAnnotation(RequestBody.class)
                .build();
    }
    
    // =========================== DATABASE COLUMN PARAMETERS ===========================
    
    /**
     * Creates parameters from database column metadata with proper type mapping.
     */
    public static List<ParameterSpec> fromColumnMetadata(List<ColumnMetadata> columns, boolean asRequestParams) {
        if (columns == null) {
            return new ArrayList<>();
        }
        
        List<ParameterSpec> parameters = new ArrayList<>();
        
        for (ColumnMetadata column : columns) {
            Class<?> javaType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            String paramName = NamingConventions.parameterName(column.getColumnName());
            
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(javaType.isPrimitive() ? 
                    ClassName.get(javaType).box() : ClassName.get(javaType), paramName);
            
            if (asRequestParams) {
                paramBuilder.addAnnotation(AnnotationSpec.builder(RequestParam.class)
                        .addMember("value", "$S", paramName)
                        .build());
            }
            
            parameters.add(paramBuilder.build());
        }
        
        return parameters;
    }
    
    /**
     * Creates parameters from DBColumn list (legacy support).
     */
    public static List<ParameterSpec> fromDBColumns(List<DBColumn> columns, boolean asRequestParams) {
        if (columns == null) {
            return new ArrayList<>();
        }
        
        List<ParameterSpec> parameters = new ArrayList<>();
        
        for (DBColumn column : columns) {
            String paramName = NamingConventions.parameterName(column.columnName());
            TypeName paramType = ClassName.bestGuess(column.javaType()).box();
            
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(paramType, paramName);
            
            if (asRequestParams) {
                paramBuilder.addAnnotation(AnnotationSpec.builder(RequestParam.class)
                        .addMember("value", "$S", paramName)
                        .build());
            }
            
            parameters.add(paramBuilder.build());
        }
        
        return parameters;
    }
    
    // =========================== SPECIALIZED PARAMETER BUILDERS ===========================
    
    /**
     * Creates a pagination parameter set (page, size, sort).
     */
    public static List<ParameterSpec> paginationParameters() {
        List<ParameterSpec> params = new ArrayList<>();
        
        params.add(optionalRequestParam(ClassName.get(Integer.class), "page", 0));
        params.add(optionalRequestParam(ClassName.get(Integer.class), "size", 20));
        params.add(optionalRequestParam(ClassName.get(String.class), "sort", null));
        
        return params;
    }
    
    /**
     * Creates a generic ID parameter for operations that need entity identification.
     */
    public static ParameterSpec idParameter(TypeName idType, String paramName) {
        validateNotNull(idType, "idType");
        validateNotEmpty(paramName, "paramName");
        
        return pathVariable(idType, paramName);
    }
    
    /**
     * Creates WHERE clause parameters for dynamic queries.
     */
    public static List<ParameterSpec> whereClauseParameters(List<ColumnMetadata> whereColumns) {
        if (whereColumns == null || whereColumns.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ParameterSpec> parameters = new ArrayList<>();
        
        for (int i = 0; i < whereColumns.size(); i++) {
            ColumnMetadata column = whereColumns.get(i);
            Class<?> paramType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            String paramName = generateWhereParamName(column, i);
            
            parameters.add(ParameterSpec.builder(paramType, paramName).build());
        }
        
        return parameters;
    }
    
    // =========================== UTILITY METHODS ===========================
    
    /**
     * Generates meaningful parameter names for WHERE clause parameters.
     */
    private static String generateWhereParamName(ColumnMetadata column, int index) {
        String columnName = column.getColumnName();
        
        // If column name is meaningful, use it
        if (!columnName.startsWith("whereParam")) {
            return NamingConventions.parameterName(columnName);
        }
        
        // Generate names based on common patterns
        return switch (index) {
            case 0 -> "id";
            case 1 -> "status";
            case 2 -> "category";
            default -> "param" + (index + 1);
        };
    }
    
    // =========================== VALIDATION HELPERS ===========================
    
    private static void validateNotEmpty(String value, String parameterName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be null or empty");
        }
    }
    
    private static void validateNotNull(Object value, String parameterName) {
        if (value == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }
}