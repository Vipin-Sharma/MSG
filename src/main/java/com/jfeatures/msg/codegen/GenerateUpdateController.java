package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import com.jfeatures.msg.codegen.util.JavaPoetTypeNameBuilder;
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
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Generates REST Controller with PUT endpoints for UPDATE operations.
 */
@Slf4j
public class GenerateUpdateController {

    private static final String ID_PARAMETER = "id";

    private GenerateUpdateController() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates controller with PUT mapping for UPDATE operations.
     */
    public static JavaFile createUpdateController(String businessPurposeOfSQL, UpdateMetadata updateMetadata) throws IOException {
        
        TypeName updateDaoTypeName = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessPurposeOfSQL, "dao", "UpdateDAO");
        TypeName updateDtoTypeName = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessPurposeOfSQL, "dto", "UpdateDTO");
        
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
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_PATH,
                                CodeGenerationConstants.STRING_PLACEHOLDER, "/api")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Tag.class)
                        .addMember("name", CodeGenerationConstants.STRING_PLACEHOLDER, businessPurposeOfSQL + " Update API")
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_DESCRIPTION,
                                CodeGenerationConstants.STRING_PLACEHOLDER,
                                "REST API for updating " + businessPurposeOfSQL.toLowerCase() + " records")
                        .build())
                .build();
        
        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "controller"), controller)
                .build();
        
        log.info(javaFile.toString());
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
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_DESCRIPTION,
                                CodeGenerationConstants.STRING_PLACEHOLDER,
                                "Updated " + businessPurposeOfSQL.toLowerCase() + " data")
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_REQUIRED,
                                CodeGenerationConstants.LITERAL_PLACEHOLDER, true)
                        .build())
                .build());
        methodCallParams.add("updateDto");
        
        // Determine if we need path variable for ID (first WHERE parameter is typically ID)
        boolean hasPathVariable = false;
        if (!updateMetadata.whereColumns().isEmpty()) {
            ColumnMetadata firstWhereColumn = updateMetadata.whereColumns().get(0);
            Class<?> idType = SQLServerDataTypeEnum.getClassForType(firstWhereColumn.getColumnTypeName());
            
            parameterSpecs.add(ParameterSpec.builder(idType, ID_PARAMETER)
                    .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                            .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_VALUE,
                                    CodeGenerationConstants.STRING_PLACEHOLDER, ID_PARAMETER)
                            .build())
                    .addAnnotation(AnnotationSpec.builder(Parameter.class)
                            .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_DESCRIPTION,
                                    CodeGenerationConstants.STRING_PLACEHOLDER, "Unique identifier")
                            .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_REQUIRED,
                                    CodeGenerationConstants.LITERAL_PLACEHOLDER, true)
                            .build())
                    .build());
            methodCallParams.add(ID_PARAMETER);
            hasPathVariable = true;
        }
        
        // Add remaining WHERE parameters as query parameters
        for (int i = 1; i < updateMetadata.whereColumns().size(); i++) {
            ColumnMetadata column = updateMetadata.whereColumns().get(i);
            Class<?> paramType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            String paramName = generateWhereParamName(column, i);
            
            parameterSpecs.add(ParameterSpec.builder(paramType, paramName)
                    .addAnnotation(AnnotationSpec.builder(RequestParam.class)
                            .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_VALUE,
                                    CodeGenerationConstants.STRING_PLACEHOLDER, paramName)
                            .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_REQUIRED,
                                    CodeGenerationConstants.LITERAL_PLACEHOLDER, true)
                            .build())
                    .addAnnotation(AnnotationSpec.builder(Parameter.class)
                            .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_DESCRIPTION,
                                    CodeGenerationConstants.STRING_PLACEHOLDER, "Filter parameter: " + paramName)
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
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_VALUE,
                                CodeGenerationConstants.STRING_PLACEHOLDER, mappingUrl)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_CONSUMES,
                                CodeGenerationConstants.STRING_PLACEHOLDER, ProjectConstants.APPLICATION_JSON)
                        .build())
                .addAnnotation(AnnotationSpec.builder(Operation.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_SUMMARY,
                                CodeGenerationConstants.STRING_PLACEHOLDER,
                                "Update " + businessPurposeOfSQL.toLowerCase() + " record")
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_DESCRIPTION,
                                CodeGenerationConstants.STRING_PLACEHOLDER,
                                "Updates an existing " + businessPurposeOfSQL.toLowerCase() + " record with the provided data")
                        .build())
                .addAnnotation(AnnotationSpec.builder(ApiResponses.class)
                        .addMember("value", """
                                {
                                    @$T(responseCode = "200", description = "Successfully updated"),
                                    @$T(responseCode = "400", description = "Invalid request data"),
                                    @$T(responseCode = "404", description = "Record not found")
                                }""", ApiResponse.class, ApiResponse.class, ApiResponse.class)
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
            case 0 -> ID_PARAMETER;
            case 1 -> "status";
            case 2 -> "category";
            default -> "param" + (index + 1);
        };
    }
}
