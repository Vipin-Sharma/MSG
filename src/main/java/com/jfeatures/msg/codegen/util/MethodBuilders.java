package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.jfeatures.msg.codegen.constants.ProjectConstants;
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

    private static final String FIELD_NAME_PARAM = "fieldName";
    private static final String DATA_SOURCE_FIELD_PARAM = "dataSourceField";
    private static final String JDBC_TEMPLATE_FIELD_PARAM = "jdbcTemplateField";
    private static final String METHOD_NAME_PARAM = "methodName";
    private static final String RETURN_TYPE_PARAM = "returnType";
    private static final String REQUEST_TYPE_PARAM = "requestType";
    private static final String PARAM_NAME_PARAM = "paramName";
    private static final String DEPENDENCY_TYPE_PARAM = "dependencyType";
    private static final String THIS_ASSIGNMENT_STATEMENT = "this.$N = $N";

    private MethodBuilders() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // =========================== CONSTRUCTOR BUILDERS ===========================
    
    /**
     * Creates a standard constructor that accepts NamedParameterJdbcTemplate.
     */
    public static MethodSpec jdbcTemplateConstructor(String fieldName) {
        validateNotEmpty(fieldName, FIELD_NAME_PARAM);
        
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(NamedParameterJdbcTemplate.class, fieldName)
                .addStatement(THIS_ASSIGNMENT_STATEMENT, fieldName, fieldName)
                .addJavadoc("Constructor with dependency injection for JDBC template.\\n@param $L the named parameter JDBC template", fieldName)
                .build();
    }
    
    /**
     * Creates a constructor with both DataSource and NamedParameterJdbcTemplate parameters.
     */
    public static MethodSpec dualParameterConstructor(String dataSourceField, String jdbcTemplateField) {
        validateNotEmpty(dataSourceField, DATA_SOURCE_FIELD_PARAM);
        validateNotEmpty(jdbcTemplateField, JDBC_TEMPLATE_FIELD_PARAM);
        
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(DataSource.class, dataSourceField)
                .addParameter(NamedParameterJdbcTemplate.class, jdbcTemplateField)
                .addStatement(THIS_ASSIGNMENT_STATEMENT, dataSourceField, dataSourceField)
                .addStatement(THIS_ASSIGNMENT_STATEMENT, jdbcTemplateField, jdbcTemplateField)
                .addJavadoc("Constructor with dependency injection.\\n@param $L the data source\\n@param $L the named parameter JDBC template", 
                           dataSourceField, jdbcTemplateField)
                .build();
    }
    
    /**
     * Creates a constructor for dependency injection in controllers.
     */
    public static MethodSpec dependencyInjectionConstructor(TypeName dependencyType, String fieldName) {
        validateNotNull(dependencyType, DEPENDENCY_TYPE_PARAM);
        validateNotEmpty(fieldName, FIELD_NAME_PARAM);
        
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(dependencyType, fieldName)
                .addStatement(THIS_ASSIGNMENT_STATEMENT, fieldName, fieldName)
                .addJavadoc("Constructor with dependency injection.\\n@param $L the injected dependency", fieldName)
                .build();
    }
    
    // =========================== REST ENDPOINT BUILDERS ===========================
    
    /**
     * Creates a GET endpoint method with comprehensive annotations.
     */
    public static MethodSpec.Builder getEndpointMethod(String methodName, TypeName returnType, String path, String summary) {
        validateNotEmpty(methodName, METHOD_NAME_PARAM);
        validateNotNull(returnType, RETURN_TYPE_PARAM);
        
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(AnnotationSpec.builder(GetMapping.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_VALUE,
                                CodeGenerationConstants.STRING_PLACEHOLDER, path != null ? path : "")
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_PRODUCES,
                                CodeGenerationConstants.STRING_PLACEHOLDER, ProjectConstants.APPLICATION_JSON)
                        .build());
        
        if (summary != null && !summary.trim().isEmpty()) {
            builder.addAnnotation(AnnotationSpec.builder(Operation.class)
                    .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_SUMMARY,
                            CodeGenerationConstants.STRING_PLACEHOLDER, summary)
                    .build());
        }
        
        return builder;
    }
    
    /**
     * Creates a POST endpoint method with validation and documentation.
     */
    public static MethodSpec.Builder postEndpointMethod(String methodName, TypeName returnType, 
                                                       TypeName requestType, String paramName, String summary) {
        validateNotEmpty(methodName, METHOD_NAME_PARAM);
        validateNotNull(returnType, RETURN_TYPE_PARAM);
        validateNotNull(requestType, REQUEST_TYPE_PARAM);
        validateNotEmpty(paramName, PARAM_NAME_PARAM);
        
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(AnnotationSpec.builder(PostMapping.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_CONSUMES,
                                CodeGenerationConstants.STRING_PLACEHOLDER, ProjectConstants.APPLICATION_JSON)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_PRODUCES,
                                CodeGenerationConstants.STRING_PLACEHOLDER, ProjectConstants.APPLICATION_JSON)
                        .build())
                .addParameter(ParameterSpec.builder(requestType, paramName)
                        .addAnnotation(Valid.class)
                        .addAnnotation(RequestBody.class)
                        .build());
        
        if (summary != null && !summary.trim().isEmpty()) {
            builder.addAnnotation(AnnotationSpec.builder(Operation.class)
                    .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_SUMMARY,
                            CodeGenerationConstants.STRING_PLACEHOLDER, summary)
                    .build());
        }
        
        return builder;
    }
    
    /**
     * Creates a PUT endpoint method for updates.
     */
    public static MethodSpec.Builder putEndpointMethod(String methodName, TypeName returnType, 
                                                      TypeName requestType, String paramName, String summary) {
        validateNotEmpty(methodName, METHOD_NAME_PARAM);
        validateNotNull(returnType, RETURN_TYPE_PARAM);
        validateNotNull(requestType, REQUEST_TYPE_PARAM);
        validateNotEmpty(paramName, PARAM_NAME_PARAM);
        
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(AnnotationSpec.builder(PutMapping.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_CONSUMES,
                                CodeGenerationConstants.STRING_PLACEHOLDER, ProjectConstants.APPLICATION_JSON)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_PRODUCES,
                                CodeGenerationConstants.STRING_PLACEHOLDER, ProjectConstants.APPLICATION_JSON)
                        .build())
                .addParameter(ParameterSpec.builder(requestType, paramName)
                        .addAnnotation(Valid.class)
                        .addAnnotation(RequestBody.class)
                        .build());
        
        if (summary != null && !summary.trim().isEmpty()) {
            builder.addAnnotation(AnnotationSpec.builder(Operation.class)
                    .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_SUMMARY,
                            CodeGenerationConstants.STRING_PLACEHOLDER, summary)
                    .build());
        }
        
        return builder;
    }
    
    /**
     * Creates a DELETE endpoint method.
     */
    public static MethodSpec.Builder deleteEndpointMethod(String methodName, TypeName returnType, String summary) {
        validateNotEmpty(methodName, METHOD_NAME_PARAM);
        validateNotNull(returnType, RETURN_TYPE_PARAM);
        
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(AnnotationSpec.builder(DeleteMapping.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_PRODUCES,
                                CodeGenerationConstants.STRING_PLACEHOLDER, ProjectConstants.APPLICATION_JSON)
                        .build());
        
        if (summary != null && !summary.trim().isEmpty()) {
            builder.addAnnotation(AnnotationSpec.builder(Operation.class)
                    .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_SUMMARY,
                            CodeGenerationConstants.STRING_PLACEHOLDER, summary)
                    .build());
        }
        
        return builder;
    }
    
    // =========================== DAO METHOD BUILDERS ===========================
    
    /**
     * Creates a basic DAO method signature.
     */
    public static MethodSpec.Builder daoMethod(String methodName, TypeName returnType, List<ParameterSpec> parameters) {
        validateNotEmpty(methodName, METHOD_NAME_PARAM);
        validateNotNull(returnType, RETURN_TYPE_PARAM);
        
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
        validateNotEmpty(paramName, PARAM_NAME_PARAM);
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
