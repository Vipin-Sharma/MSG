package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import com.jfeatures.msg.codegen.util.JavaPoetTypeNameBuilder;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.text.CaseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import jakarta.validation.Valid;
import java.io.IOException;

/**
 * Generates REST Controller with POST endpoints for INSERT operations.
 * Following Vipin's Principle: Single responsibility - INSERT controller generation only.
 */
public class GenerateInsertController {
    
    /**
     * Creates controller with POST mapping for INSERT operations.
     * Single responsibility: Generate INSERT REST controller.
     */
    public static JavaFile createInsertController(String businessPurposeOfSQL, InsertMetadata insertMetadata) throws IOException {
        
        if (businessPurposeOfSQL == null || businessPurposeOfSQL.trim().isEmpty()) {
            throw new IllegalArgumentException("Business purpose of SQL cannot be null or empty");
        }
        if (insertMetadata == null) {
            throw new IllegalArgumentException("Insert metadata cannot be null");
        }
        
        TypeName insertDaoTypeName = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessPurposeOfSQL, "dao", "InsertDAO");
        TypeName insertDtoTypeName = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessPurposeOfSQL, "dto", "InsertDTO");
        
        String daoInstanceFieldName = CaseUtils.toCamelCase(businessPurposeOfSQL, false) + "InsertDAO";
        
        // DAO field injection
        FieldSpec daoFieldSpec = FieldSpec.builder(insertDaoTypeName, daoInstanceFieldName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
        
        // Constructor for dependency injection
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(insertDaoTypeName, daoInstanceFieldName)
                .addStatement("this.$N = $N", daoInstanceFieldName, daoInstanceFieldName)
                .build();
        
        // POST endpoint method
        MethodSpec insertMethodSpec = MethodSpec.methodBuilder("create" + businessPurposeOfSQL)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(PostMapping.class)
                        .addMember("value", "$S", "/" + businessPurposeOfSQL.toLowerCase())
                        .addMember("consumes", "$S", "application/json")
                        .addMember("produces", "$S", "application/json")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Operation.class)
                        .addMember("summary", "$S", "Create new " + businessPurposeOfSQL.toLowerCase() + " entity")
                        .addMember("description", "$S", "POST API to create a new " + businessPurposeOfSQL.toLowerCase() + " record")
                        .build())
                .addParameter(ParameterSpec.builder(insertDtoTypeName, "insertRequest")
                        .addAnnotation(AnnotationSpec.builder(Valid.class).build())
                        .addAnnotation(AnnotationSpec.builder(RequestBody.class).build())
                        .build())
                .returns(ClassName.get("org.springframework.http", "ResponseEntity").withoutAnnotations())
                .addStatement("int rowsAffected = $N.insert$L(insertRequest)", daoInstanceFieldName, businessPurposeOfSQL)
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if (rowsAffected > 0)")
                        .addStatement("return $T.status($T.CREATED).body($S)", 
                                ResponseEntity.class, HttpStatus.class, businessPurposeOfSQL.toLowerCase() + " created successfully")
                        .nextControlFlow("else")
                        .addStatement("return $T.status($T.INTERNAL_SERVER_ERROR).body($S)", 
                                ResponseEntity.class, HttpStatus.class, "Failed to create " + businessPurposeOfSQL.toLowerCase())
                        .endControlFlow()
                        .build())
                .build();
        
        // Controller class
        TypeSpec controller = TypeSpec.classBuilder(businessPurposeOfSQL + "InsertController")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(RestController.class).build())
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("path", "$S", "/api")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Tag.class)
                        .addMember("name", "$S", businessPurposeOfSQL)
                        .addMember("description", "$S", businessPurposeOfSQL + " INSERT operations")
                        .build())
                .addField(daoFieldSpec)
                .addMethod(constructorSpec)
                .addMethod(insertMethodSpec)
                .build();
        
        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "controller"), controller)
                .build();
        
        javaFile.writeTo(System.out);
        
        return javaFile;
    }
}