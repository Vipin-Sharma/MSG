package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.codegen.util.TypeUtil;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.text.CaseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates REST Controller with PUT endpoints for UPDATE operations.
 */
public class GenerateUpdateController {
    
    /**
     * Creates controller with PUT mapping for UPDATE operations.
     */
    public static JavaFile createUpdateController(String businessPurposeOfSQL, UpdateMetadata updateMetadata) throws IOException {
        
        TypeName updateDaoTypeName = TypeUtil.getJavaClassTypeName(businessPurposeOfSQL, "dao", "UpdateDAO");
        TypeName updateDtoTypeName = TypeUtil.getJavaClassTypeName(businessPurposeOfSQL, "dto", "UpdateDTO");
        
        String daoInstanceFieldName = CaseUtils.toCamelCase(businessPurposeOfSQL, false) + "UpdateDAO";
        
        // DAO field
        FieldSpec daoFieldSpec = FieldSpec.builder(updateDaoTypeName, daoInstanceFieldName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
        
        // Constructor
        CodeBlock constructorCode = CodeBlock.builder()
                .addStatement("this.$N = $N", daoInstanceFieldName, daoInstanceFieldName)
                .build();
        
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(updateDaoTypeName, daoInstanceFieldName)
                .addCode(constructorCode)
                .build();
        
        // Generate PUT method
        MethodSpec putMethod = createPutMethod(businessPurposeOfSQL, updateMetadata, updateDtoTypeName, daoInstanceFieldName);
        
        // Create controller class
        TypeSpec controller = TypeSpec.classBuilder(businessPurposeOfSQL + "UpdateController")
                .addModifiers(Modifier.PUBLIC)
                .addField(daoFieldSpec)
                .addMethod(constructorSpec)
                .addMethod(putMethod)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("path", "$S", "/api")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Tag.class)
                        .addMember("name", "$S", businessPurposeOfSQL + " Update API")
                        .addMember("description", "$S", "REST API for updating " + businessPurposeOfSQL.toLowerCase() + " records")
                        .build())
                .build();
        
        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "controller"), controller)
                .build();
        
        javaFile.writeTo(System.out);
        return javaFile;
    }
    
    /**
     * Creates the PUT method for UPDATE operations.
     */
    private static MethodSpec createPutMethod(String businessPurposeOfSQL, UpdateMetadata updateMetadata, 
                                            TypeName updateDtoTypeName, String daoInstanceFieldName) {
        
        // Build parameter specifications
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        List<String> methodCallParams = new ArrayList<>();
        
        // Add request body parameter (DTO)
        parameterSpecs.add(ParameterSpec.builder(updateDtoTypeName, "updateDto")
                .addAnnotation(Valid.class)
                .addAnnotation(RequestBody.class)
                .addAnnotation(AnnotationSpec.builder(Parameter.class)
                        .addMember("description", "$S", "Updated " + businessPurposeOfSQL.toLowerCase() + " data")
                        .addMember("required", "true")
                        .build())
                .build());
        methodCallParams.add("updateDto");
        
        // Determine if we need path variable for ID (first WHERE parameter is typically ID)
        boolean hasPathVariable = false;
        if (!updateMetadata.whereColumns().isEmpty()) {
            ColumnMetadata firstWhereColumn = updateMetadata.whereColumns().get(0);
            Class<?> idType = SQLServerDataTypeEnum.getClassForType(firstWhereColumn.getColumnTypeName());
            
            parameterSpecs.add(ParameterSpec.builder(idType, "id")
                    .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                            .addMember("value", "$S", "id")
                            .build())
                    .addAnnotation(AnnotationSpec.builder(Parameter.class)
                            .addMember("description", "$S", "Unique identifier")
                            .addMember("required", "true")
                            .build())
                    .build());
            methodCallParams.add("id");
            hasPathVariable = true;
        }
        
        // Add remaining WHERE parameters as query parameters
        for (int i = 1; i < updateMetadata.whereColumns().size(); i++) {
            ColumnMetadata column = updateMetadata.whereColumns().get(i);
            Class<?> paramType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            String paramName = generateWhereParamName(column, i);
            
            parameterSpecs.add(ParameterSpec.builder(paramType, paramName)
                    .addAnnotation(AnnotationSpec.builder(RequestParam.class)
                            .addMember("value", "$S", paramName)
                            .addMember("required", "true")
                            .build())
                    .addAnnotation(AnnotationSpec.builder(Parameter.class)
                            .addMember("description", "$S", "Filter parameter: " + paramName)
                            .build())
                    .build());
            methodCallParams.add(paramName);
        }
        
        // Build method call parameters string
        String methodCallParamsString = String.join(", ", methodCallParams);
        
        // Build method body
        CodeBlock methodBody = CodeBlock.builder()
                .addStatement("int rowsUpdated = $N.update$N($N)", daoInstanceFieldName, businessPurposeOfSQL, methodCallParamsString)
                .add("\n")
                .beginControlFlow("if (rowsUpdated > 0)")
                .addStatement("return $T.ok().build()", ResponseEntity.class)
                .nextControlFlow("else")
                .addStatement("return $T.notFound().build()", ResponseEntity.class)
                .endControlFlow()
                .build();
        
        // Determine URL mapping
        String mappingUrl = hasPathVariable ? 
            "/" + businessPurposeOfSQL.toLowerCase() + "/{id}" : 
            "/" + businessPurposeOfSQL.toLowerCase();
        
        return MethodSpec.methodBuilder("update" + businessPurposeOfSQL)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecs)
                .returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ClassName.get(Void.class)))
                .addAnnotation(AnnotationSpec.builder(PutMapping.class)
                        .addMember("value", "$S", mappingUrl)
                        .addMember("consumes", "$S", "application/json")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Operation.class)
                        .addMember("summary", "$S", "Update " + businessPurposeOfSQL.toLowerCase() + " record")
                        .addMember("description", "$S", "Updates an existing " + businessPurposeOfSQL.toLowerCase() + " record with the provided data")
                        .build())
                .addAnnotation(AnnotationSpec.builder(ApiResponses.class)
                        .addMember("value", "{\n" +
                                "    @$T(responseCode = \"200\", description = \"Successfully updated\"),\n" +
                                "    @$T(responseCode = \"400\", description = \"Invalid request data\"),\n" +
                                "    @$T(responseCode = \"404\", description = \"Record not found\")\n" +
                                "}", ApiResponse.class, ApiResponse.class, ApiResponse.class)
                        .build())
                .addCode(methodBody)
                .addJavadoc("Updates a $L record.\n" +
                           "@param updateDto The updated data\n" +
                           (hasPathVariable ? "@param id The record identifier\n" : "") +
                           "@return ResponseEntity indicating success or failure",
                           businessPurposeOfSQL.toLowerCase())
                .build();
    }
    
    /**
     * Generates meaningful parameter names for WHERE clause parameters.
     */
    private static String generateWhereParamName(ColumnMetadata column, int index) {
        String columnName = column.getColumnName();
        
        // If column name is meaningful, use it
        if (!columnName.startsWith("whereParam")) {
            return CaseUtils.toCamelCase(columnName, false, '_');
        }
        
        // Generate names based on common patterns
        return switch (index) {
            case 0 -> "id";
            case 1 -> "status";
            case 2 -> "category";
            default -> "param" + (index + 1);
        };
    }
}