package com.jfeatures.msg.codegen.util;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * Specialized builder for JavaPoet method specifications.
 * Follows Single Responsibility Principle - only method creation logic.
 */
public final class MethodBuilders {
    
    private MethodBuilders() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // =========================== CONSTRUCTOR BUILDERS ===========================
    
    /**
     * Creates a standard constructor that accepts NamedParameterJdbcTemplate.
     */
    public static MethodSpec jdbcTemplateConstructor(String fieldName) {
        validateNotEmpty(fieldName, "fieldName");
        
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(NamedParameterJdbcTemplate.class, fieldName)
                .addStatement("this.$N = $N", fieldName, fieldName)
                .addJavadoc("Constructor with dependency injection for JDBC template.\\n@param $L the named parameter JDBC template", fieldName)
                .build();
    }
    
    /**
     * Creates a constructor with both DataSource and NamedParameterJdbcTemplate parameters.
     */
    public static MethodSpec dualParameterConstructor(String dataSourceField, String jdbcTemplateField) {
        validateNotEmpty(dataSourceField, "dataSourceField");
        validateNotEmpty(jdbcTemplateField, "jdbcTemplateField");
        
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(DataSource.class, dataSourceField)
                .addParameter(NamedParameterJdbcTemplate.class, jdbcTemplateField)
                .addStatement("this.$N = $N", dataSourceField, dataSourceField)
                .addStatement("this.$N = $N", jdbcTemplateField, jdbcTemplateField)
                .addJavadoc("Constructor with dependency injection.\\n@param $L the data source\\n@param $L the named parameter JDBC template", 
                           dataSourceField, jdbcTemplateField)
                .build();
    }
    
    /**
     * Creates a constructor for dependency injection in controllers.
     */
    public static MethodSpec dependencyInjectionConstructor(TypeName dependencyType, String fieldName) {
        validateNotNull(dependencyType, "dependencyType");
        validateNotEmpty(fieldName, "fieldName");
        
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(dependencyType, fieldName)
                .addStatement("this.$N = $N", fieldName, fieldName)
                .addJavadoc("Constructor with dependency injection.\\n@param $L the injected dependency", fieldName)
                .build();
    }
    
    // =========================== REST ENDPOINT BUILDERS ===========================
    
    /**
     * Creates a GET endpoint method with comprehensive annotations.
     */
    public static MethodSpec.Builder getEndpointMethod(String methodName, TypeName returnType, String path, String summary) {
        validateNotEmpty(methodName, "methodName");
        validateNotNull(returnType, "returnType");
        
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(AnnotationSpec.builder(GetMapping.class)
                        .addMember("value", "$S", path != null ? path : "")
                        .addMember("produces", "$S", "application/json")
                        .build());
        
        if (summary != null && !summary.trim().isEmpty()) {
            builder.addAnnotation(AnnotationSpec.builder(Operation.class)
                    .addMember("summary", "$S", summary)
                    .build());
        }
        
        return builder;
    }
    
    /**
     * Creates a POST endpoint method with validation and documentation.
     */
    public static MethodSpec.Builder postEndpointMethod(String methodName, TypeName returnType, 
                                                       TypeName requestType, String paramName, String summary) {
        validateNotEmpty(methodName, "methodName");
        validateNotNull(returnType, "returnType");
        validateNotNull(requestType, "requestType");
        validateNotEmpty(paramName, "paramName");
        
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(AnnotationSpec.builder(PostMapping.class)
                        .addMember("consumes", "$S", "application/json")
                        .addMember("produces", "$S", "application/json")
                        .build())
                .addParameter(ParameterSpec.builder(requestType, paramName)
                        .addAnnotation(Valid.class)
                        .addAnnotation(RequestBody.class)
                        .build());
        
        if (summary != null && !summary.trim().isEmpty()) {
            builder.addAnnotation(AnnotationSpec.builder(Operation.class)
                    .addMember("summary", "$S", summary)
                    .build());
        }
        
        return builder;
    }
    
    /**
     * Creates a PUT endpoint method for updates.
     */
    public static MethodSpec.Builder putEndpointMethod(String methodName, TypeName returnType, 
                                                      TypeName requestType, String paramName, String summary) {
        validateNotEmpty(methodName, "methodName");
        validateNotNull(returnType, "returnType");
        validateNotNull(requestType, "requestType");
        validateNotEmpty(paramName, "paramName");
        
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(AnnotationSpec.builder(PutMapping.class)
                        .addMember("consumes", "$S", "application/json")
                        .addMember("produces", "$S", "application/json")
                        .build())
                .addParameter(ParameterSpec.builder(requestType, paramName)
                        .addAnnotation(Valid.class)
                        .addAnnotation(RequestBody.class)
                        .build());
        
        if (summary != null && !summary.trim().isEmpty()) {
            builder.addAnnotation(AnnotationSpec.builder(Operation.class)
                    .addMember("summary", "$S", summary)
                    .build());
        }
        
        return builder;
    }
    
    /**
     * Creates a DELETE endpoint method.
     */
    public static MethodSpec.Builder deleteEndpointMethod(String methodName, TypeName returnType, String summary) {
        validateNotEmpty(methodName, "methodName");
        validateNotNull(returnType, "returnType");
        
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(AnnotationSpec.builder(DeleteMapping.class)
                        .addMember("produces", "$S", "application/json")
                        .build());
        
        if (summary != null && !summary.trim().isEmpty()) {
            builder.addAnnotation(AnnotationSpec.builder(Operation.class)
                    .addMember("summary", "$S", summary)
                    .build());
        }
        
        return builder;
    }
    
    // =========================== DAO METHOD BUILDERS ===========================
    
    /**
     * Creates a basic DAO method signature.
     */
    public static MethodSpec.Builder daoMethod(String methodName, TypeName returnType, List<ParameterSpec> parameters) {
        validateNotEmpty(methodName, "methodName");
        validateNotNull(returnType, "returnType");
        
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType);
        
        if (parameters != null && !parameters.isEmpty()) {
            builder.addParameters(parameters);
        }
        
        return builder;
    }
    
    /**
     * Creates a parameterized DAO method with parameter map handling.
     */
    public static CodeBlock parameterMapCreation() {
        return CodeBlock.builder()
                .addStatement("$T<$T, $T> paramMap = new $T<>()", 
                             java.util.Map.class, String.class, Object.class, java.util.HashMap.class)
                .build();
    }
    
    /**
     * Creates parameter mapping code for a given parameter.
     */
    public static CodeBlock parameterMapping(String paramName, String paramValue) {
        validateNotEmpty(paramName, "paramName");
        validateNotEmpty(paramValue, "paramValue");
        
        return CodeBlock.builder()
                .addStatement("paramMap.put($S, $L)", paramName, paramValue)
                .build();
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