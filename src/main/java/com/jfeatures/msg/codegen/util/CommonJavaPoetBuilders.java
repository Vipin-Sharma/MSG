package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.squareup.javapoet.*;
import jakarta.validation.Valid;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * Common JavaPoet builders to eliminate code duplication across all generators.
 * Provides standardized field, method, and class builders for consistent code generation.
 */
public final class CommonJavaPoetBuilders {

    private static final String THIS_ASSIGNMENT_STATEMENT = "this.$N = $N";

    private CommonJavaPoetBuilders() {
        // utility class
    }
    
    // =========================== FIELD BUILDERS ===========================
    
    /**
     * Creates a private final NamedParameterJdbcTemplate field.
     */
    public static FieldSpec jdbcTemplateField(String fieldName) {
        return FieldSpec.builder(NamedParameterJdbcTemplate.class, fieldName, 
                Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }
    
    /**
     * Creates a private final SQL string field.
     */
    public static FieldSpec sqlField(String sql, String businessName) {
        String fieldName = businessName.toLowerCase() + "Sql";
        return FieldSpec.builder(String.class, fieldName,
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(CodeGenerationConstants.STRING_PLACEHOLDER, sql)
                .build();
    }
    
    /**
     * Creates a private static final SQL field with custom name.
     */
    public static FieldSpec sqlFieldWithName(String sql, String fieldName) {
        return FieldSpec.builder(String.class, fieldName, 
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"\"\"\n$L\"\"\"", sql)
                .build();
    }
    
    /**
     * Creates a private static final SQL field with custom name and JavaDoc.
     */
    public static FieldSpec sqlFieldWithNameAndJavaDoc(String sql, String fieldName, String businessName) {
        return FieldSpec.builder(String.class, fieldName, 
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"\"\"\n$L\"\"\"", sql)
                .addJavadoc("SQL statement for $L operations", businessName.toLowerCase())
                .build();
    }
    
    // =========================== METHOD BUILDERS ===========================
    
    /**
     * Creates a standard constructor that accepts NamedParameterJdbcTemplate.
     */
    public static MethodSpec jdbcTemplateConstructor(String fieldName) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(NamedParameterJdbcTemplate.class, fieldName)
                .addStatement(THIS_ASSIGNMENT_STATEMENT, fieldName, fieldName)
                .build();
    }
    
    /**
     * Creates a constructor with both DataSource and NamedParameterJdbcTemplate parameters.
     */
    public static MethodSpec dualParameterConstructor(String dataSourceField, String jdbcTemplateField) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(DataSource.class, dataSourceField)
                .addParameter(NamedParameterJdbcTemplate.class, jdbcTemplateField)
                .addStatement(THIS_ASSIGNMENT_STATEMENT, dataSourceField, dataSourceField)
                .addStatement(THIS_ASSIGNMENT_STATEMENT, jdbcTemplateField, jdbcTemplateField)
                .build();
    }
    
    // =========================== CLASS BUILDERS ===========================
    
    /**
     * Creates a basic @Component annotated DAO class with NamedParameterJdbcTemplate.
     */
    public static TypeSpec.Builder basicDAOClass(String businessName, String jdbcTemplateFieldName) {
        String className = businessName + "DAO";
        
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class)
                .addField(jdbcTemplateField(jdbcTemplateFieldName))
                .addMethod(jdbcTemplateConstructor(jdbcTemplateFieldName));
    }
    
    /**
     * Creates a basic REST controller class with standard annotations.
     */
    public static TypeSpec.Builder basicControllerClass(String businessName, String basePath) {
        String className = businessName + "Controller";
        
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_PATH,
                                CodeGenerationConstants.STRING_PLACEHOLDER, basePath)
                        .build());
    }
    
    /**
     * Creates a basic DTO class (public class with no annotations).
     */
    public static TypeSpec.Builder basicDTOClass(String businessName, String suffix) {
        String className = businessName + suffix;
        
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC);
    }
    
    // =========================== COMMON METHOD PATTERNS ===========================
    
    /**
     * Creates a standard GET endpoint method signature.
     */
    public static MethodSpec.Builder getEndpointMethod(String methodName, TypeName returnType) {
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(GetMapping.class);
    }
    
    /**
     * Creates a standard POST endpoint method signature.
     */
    public static MethodSpec.Builder postEndpointMethod(String methodName, TypeName returnType, TypeName paramType, String paramName) {
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(PostMapping.class)
                .addParameter(ParameterSpec.builder(paramType, paramName)
                        .addAnnotation(Valid.class)
                        .addAnnotation(RequestBody.class)
                        .build());
    }
    
    /**
     * Creates a standard PUT endpoint method signature.
     */
    public static MethodSpec.Builder putEndpointMethod(String methodName, TypeName returnType, TypeName paramType, String paramName) {
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(PutMapping.class)
                .addParameter(ParameterSpec.builder(paramType, paramName)
                        .addAnnotation(Valid.class)
                        .addAnnotation(RequestBody.class)
                        .build());
    }
    
    /**
     * Creates a standard DELETE endpoint method signature.
     */
    public static MethodSpec.Builder deleteEndpointMethod(String methodName, TypeName returnType) {
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(DeleteMapping.class);
    }
    
    // =========================== COMMON PARAMETER PATTERNS ===========================
    
    /**
     * Creates a @RequestParam parameter.
     */
    public static ParameterSpec requestParam(TypeName type, String name) {
        return ParameterSpec.builder(type, name)
                .addAnnotation(RequestParam.class)
                .build();
    }
    
    /**
     * Creates a @RequestParam parameter with custom name.
     */
    public static ParameterSpec requestParamWithName(TypeName type, String paramName, String requestParamName) {
        return ParameterSpec.builder(type, paramName)
                .addAnnotation(AnnotationSpec.builder(RequestParam.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_VALUE,
                                CodeGenerationConstants.STRING_PLACEHOLDER, requestParamName)
                        .build())
                .build();
    }
    
    /**
     * Creates a @PathVariable parameter.
     */
    public static ParameterSpec pathVariable(TypeName type, String name) {
        return ParameterSpec.builder(type, name)
                .addAnnotation(PathVariable.class)
                .build();
    }
    
}