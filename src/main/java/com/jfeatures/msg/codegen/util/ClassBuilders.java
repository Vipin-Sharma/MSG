package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.lang.model.element.Modifier;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Specialized builder for JavaPoet class specifications.
 * Follows Single Responsibility Principle - only class creation logic.
 */
public final class ClassBuilders {
    
    private ClassBuilders() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // =========================== DAO CLASS BUILDERS ===========================
    
    /**
     * Creates a basic DAO class with @Component annotation and logging.
     */
    public static TypeSpec.Builder basicDAOClass(String businessName) {
        validateNotEmpty(businessName, ProjectConstants.BUSINESS_NAME_PARAM);
        
        String className = NamingConventions.daoClassName(businessName);
        
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class)
                .addAnnotation(Slf4j.class)
                .addJavadoc("Data Access Object for $L operations.\\nFollows single responsibility principle.", businessName.toLowerCase());
    }
    
    /**
     * Creates a specialized DAO class for specific operations (Insert, Update, Delete).
     */
    public static TypeSpec.Builder operationDAOClass(String businessName, String operation) {
        validateNotEmpty(businessName, ProjectConstants.BUSINESS_NAME_PARAM);
        validateNotEmpty(operation, ProjectConstants.OPERATION_PARAM);
        
        String className = NamingConventions.buildClassName(businessName, operation + "DAO");
        
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class)
                .addAnnotation(Slf4j.class)
                .addJavadoc("Data Access Object for $L $L operations.\\nFollows single responsibility principle - $L operations only.", 
                           businessName.toLowerCase(), operation.toLowerCase(), operation.toLowerCase());
    }
    
    // =========================== CONTROLLER CLASS BUILDERS ===========================
    
    /**
     * Creates a basic REST controller class with standard annotations.
     */
    public static TypeSpec.Builder basicControllerClass(String businessName, String basePath) {
        validateNotEmpty(businessName, ProjectConstants.BUSINESS_NAME_PARAM);
        
        String className = NamingConventions.controllerClassName(businessName);
        String apiPath = basePath != null ? basePath : "/api";
        
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_PATH,
                                CodeGenerationConstants.STRING_PLACEHOLDER, apiPath)
                        .build())
                .addAnnotation(AnnotationSpec.builder(Tag.class)
                        .addMember("name", CodeGenerationConstants.STRING_PLACEHOLDER, businessName)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_DESCRIPTION,
                                CodeGenerationConstants.STRING_PLACEHOLDER,
                                "REST API for " + businessName.toLowerCase() + " operations")
                        .build())
                .addJavadoc("REST Controller for $L operations.\\nProvides HTTP endpoints for data access.", businessName.toLowerCase());
    }
    
    /**
     * Creates a specialized controller for specific operations.
     */
    public static TypeSpec.Builder operationControllerClass(String businessName, String operation, String basePath) {
        validateNotEmpty(businessName, ProjectConstants.BUSINESS_NAME_PARAM);
        validateNotEmpty(operation, ProjectConstants.OPERATION_PARAM);
        
        String className = NamingConventions.buildClassName(businessName, operation + "Controller");
        String apiPath = basePath != null ? basePath : "/api";
        
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_PATH,
                                CodeGenerationConstants.STRING_PLACEHOLDER, apiPath)
                        .build())
                .addAnnotation(AnnotationSpec.builder(Tag.class)
                        .addMember("name", CodeGenerationConstants.STRING_PLACEHOLDER, businessName + " " + operation)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_DESCRIPTION,
                                CodeGenerationConstants.STRING_PLACEHOLDER,
                                operation + " operations for " + businessName.toLowerCase())
                        .build())
                .addJavadoc("REST Controller for $L $L operations.\\nFollows single responsibility principle - $L operations only.", 
                           businessName.toLowerCase(), operation.toLowerCase(), operation.toLowerCase());
    }
    
    // =========================== DTO CLASS BUILDERS ===========================
    
    /**
     * Creates a basic DTO class with Lombok annotations.
     */
    public static TypeSpec.Builder basicDTOClass(String businessName) {
        validateNotEmpty(businessName, ProjectConstants.BUSINESS_NAME_PARAM);
        
        String className = NamingConventions.dtoClassName(businessName);
        
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Builder.class)
                        .addMember("builderClassName", CodeGenerationConstants.STRING_PLACEHOLDER,
                                CodeGenerationConstants.BUILDER_VARIABLE_NAME)
                        .build())
                .addAnnotation(Value.class)
                .addAnnotation(Jacksonized.class)
                .addJavadoc("Data Transfer Object for $L.\\nImmutable class with builder pattern support.", businessName.toLowerCase());
    }
    
    /**
     * Creates a specialized DTO for specific operations (Insert, Update, etc.).
     */
    public static TypeSpec.Builder operationDTOClass(String businessName, String operation) {
        validateNotEmpty(businessName, ProjectConstants.BUSINESS_NAME_PARAM);
        validateNotEmpty(operation, ProjectConstants.OPERATION_PARAM);
        
        String className = NamingConventions.buildClassName(businessName, operation + "DTO");
        
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Builder.class)
                        .addMember("builderClassName", CodeGenerationConstants.STRING_PLACEHOLDER,
                                CodeGenerationConstants.BUILDER_VARIABLE_NAME)
                        .build())
                .addAnnotation(Value.class)
                .addAnnotation(Jacksonized.class)
                .addJavadoc("Data Transfer Object for $L $L operations.\\nImmutable class with builder pattern support.", 
                           businessName.toLowerCase(), operation.toLowerCase());
    }
    
    /**
     * Creates a simple POJO DTO class (for cases where Lombok builder hits field limits).
     */
    public static TypeSpec.Builder pojoDTOClass(String businessName) {
        validateNotEmpty(businessName, ProjectConstants.BUSINESS_NAME_PARAM);
        
        String className = NamingConventions.dtoClassName(businessName);
        
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Data Transfer Object for $L.\\nStandard POJO implementation due to field count limitations.", businessName.toLowerCase());
    }
    
    // =========================== CONFIGURATION CLASS BUILDERS ===========================
    
    /**
     * Creates a Spring Boot configuration class.
     */
    public static TypeSpec.Builder configurationClass(String configName) {
        validateNotEmpty(configName, "configName");
        
        return TypeSpec.classBuilder(configName + "Config")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(org.springframework.context.annotation.Configuration.class)
                .addJavadoc("Configuration class for $L.\\nDefines beans and configuration settings.", configName.toLowerCase());
    }
    
    // =========================== VALIDATION HELPERS ===========================
    
    private static void validateNotEmpty(String value, String parameterName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be null or empty");
        }
    }
}