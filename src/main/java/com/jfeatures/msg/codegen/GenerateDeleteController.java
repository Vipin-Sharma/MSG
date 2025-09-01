package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.DeleteMetadata;
import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import com.jfeatures.msg.codegen.util.JavaPoetTypeNameBuilder;
import com.jfeatures.msg.codegen.SQLServerDataTypeEnum;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates REST Controller with DELETE endpoints for DELETE operations.
 * Following Vipin's Principle: Single responsibility - DELETE controller generation only.
 */
public class GenerateDeleteController {
    
    /**
     * Creates controller with DELETE mapping for DELETE operations.
     * Single responsibility: Generate DELETE REST controller.
     */
    public static JavaFile createDeleteController(String businessPurposeOfSQL, DeleteMetadata deleteMetadata) throws IOException {
        
        if (businessPurposeOfSQL == null || businessPurposeOfSQL.trim().isEmpty()) {
            throw new IllegalArgumentException("Business purpose of SQL cannot be null or empty");
        }
        if (deleteMetadata == null) {
            throw new IllegalArgumentException("Delete metadata cannot be null");
        }
        if (deleteMetadata.whereColumns().isEmpty()) {
            throw new IllegalArgumentException("Delete metadata must have at least one WHERE column");
        }
        
        TypeName deleteDaoTypeName = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessPurposeOfSQL, "dao", "DeleteDAO");
        
        String daoInstanceFieldName = CaseUtils.toCamelCase(businessPurposeOfSQL, false) + "DeleteDAO";
        
        // DAO field injection
        FieldSpec daoFieldSpec = FieldSpec.builder(deleteDaoTypeName, daoInstanceFieldName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
        
        // Constructor for dependency injection
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(deleteDaoTypeName, daoInstanceFieldName)
                .addStatement("this.$N = $N", daoInstanceFieldName, daoInstanceFieldName)
                .build();
        
        // Generate method parameters for WHERE clause columns
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        List<String> parameterNames = new ArrayList<>();
        
        for (ColumnMetadata column : deleteMetadata.whereColumns()) {
            Class<?> paramType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            String paramName = CaseUtils.toCamelCase(column.getColumnName(), false);
            
            ParameterSpec paramSpec = ParameterSpec.builder(paramType, paramName)
                    .addAnnotation(AnnotationSpec.builder(RequestParam.class)
                            .addMember("value", "$S", paramName.toLowerCase())
                            .addMember("required", "$L", true)
                            .build())
                    .build();
            
            parameterSpecs.add(paramSpec);
            parameterNames.add(paramName);
        }
        
        // DELETE endpoint method
        MethodSpec.Builder deleteMethodBuilder = MethodSpec.methodBuilder("delete" + businessPurposeOfSQL)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(DeleteMapping.class)
                        .addMember("value", "$S", "/" + businessPurposeOfSQL.toLowerCase())
                        .addMember("produces", "$S", "application/json")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Operation.class)
                        .addMember("summary", "$S", "Delete " + businessPurposeOfSQL.toLowerCase() + " entity")
                        .addMember("description", "$S", "DELETE API to remove a " + businessPurposeOfSQL.toLowerCase() + " record")
                        .build())
                .returns(ClassName.get("org.springframework.http", "ResponseEntity").withoutAnnotations());
        
        // Add all parameters
        for (ParameterSpec paramSpec : parameterSpecs) {
            deleteMethodBuilder.addParameter(paramSpec);
        }
        
        // Method body - call DAO with parameters
        StringBuilder daoCallBuilder = new StringBuilder();
        daoCallBuilder.append("int rowsAffected = $N.delete$L(");
        daoCallBuilder.append(String.join(", ", parameterNames));
        daoCallBuilder.append(")");
        
        deleteMethodBuilder.addStatement(daoCallBuilder.toString(), daoInstanceFieldName, businessPurposeOfSQL)
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if (rowsAffected > 0)")
                        .addStatement("return $T.status($T.NO_CONTENT).body($S)", 
                                ResponseEntity.class, HttpStatus.class, businessPurposeOfSQL.toLowerCase() + " deleted successfully")
                        .nextControlFlow("else")
                        .addStatement("return $T.status($T.NOT_FOUND).body($S)", 
                                ResponseEntity.class, HttpStatus.class, businessPurposeOfSQL.toLowerCase() + " not found")
                        .endControlFlow()
                        .build());
        
        MethodSpec deleteMethodSpec = deleteMethodBuilder.build();
        
        // Controller class
        TypeSpec controller = TypeSpec.classBuilder(businessPurposeOfSQL + "DeleteController")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(RestController.class).build())
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("path", "$S", "/api")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Tag.class)
                        .addMember("name", "$S", businessPurposeOfSQL)
                        .addMember("description", "$S", businessPurposeOfSQL + " DELETE operations")
                        .build())
                .addField(daoFieldSpec)
                .addMethod(constructorSpec)
                .addMethod(deleteMethodSpec)
                .build();
        
        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "controller"), controller)
                .build();
        
        javaFile.writeTo(System.out);
        
        return javaFile;
    }
}