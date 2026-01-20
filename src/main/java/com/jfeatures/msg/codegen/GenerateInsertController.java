package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.domain.AuditConfiguration;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import javax.lang.model.element.Modifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Generates REST Controller with POST endpoints for INSERT operations.
 * Following Vipin's Principle: Single responsibility - INSERT controller generation only.
 */
@Slf4j
public class GenerateInsertController {

    private GenerateInsertController() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates controller with POST mapping for INSERT operations without audit support.
     * Single responsibility: Generate INSERT REST controller.
     */
    public static JavaFile createInsertController(String businessPurposeOfSQL, InsertMetadata insertMetadata) throws IOException {
        return createInsertController(businessPurposeOfSQL, insertMetadata, AuditConfiguration.disabled());
    }

    /**
     * Creates controller with POST mapping for INSERT operations with optional audit support.
     * When audit is enabled, adds a @RequestHeader parameter to capture user identity.
     *
     * @param businessPurposeOfSQL The business domain name (e.g., "Customer")
     * @param insertMetadata The INSERT metadata from SQL
     * @param auditConfig Audit configuration for user context support
     * @return Generated JavaFile containing the controller class
     * @throws IOException if file generation fails
     */
    public static JavaFile createInsertController(String businessPurposeOfSQL, InsertMetadata insertMetadata,
                                                  AuditConfiguration auditConfig) throws IOException {

        if (businessPurposeOfSQL == null || businessPurposeOfSQL.trim().isEmpty()) {
            throw new IllegalArgumentException("Business purpose of SQL cannot be null or empty");
        }
        if (insertMetadata == null) {
            throw new IllegalArgumentException("Insert metadata cannot be null");
        }
        if (auditConfig == null) {
            auditConfig = AuditConfiguration.disabled();
        }

        TypeName insertDaoTypeName = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessPurposeOfSQL, "dao", "InsertDAO");
        TypeName insertDtoTypeName = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessPurposeOfSQL, "dto", "InsertDTO");

        String daoInstanceFieldName = CaseUtils.toCamelCase(businessPurposeOfSQL, false) + "InsertDAO";

        // Build the POST endpoint method
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("create" + businessPurposeOfSQL)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(PostMapping.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_VALUE,
                                CodeGenerationConstants.STRING_PLACEHOLDER, "/" + businessPurposeOfSQL.toLowerCase())
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_CONSUMES,
                                CodeGenerationConstants.STRING_PLACEHOLDER, ProjectConstants.APPLICATION_JSON)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_PRODUCES,
                                CodeGenerationConstants.STRING_PLACEHOLDER, ProjectConstants.APPLICATION_JSON)
                        .build())
                .addAnnotation(AnnotationSpec.builder(Operation.class)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_SUMMARY,
                                CodeGenerationConstants.STRING_PLACEHOLDER,
                                "Create new " + businessPurposeOfSQL.toLowerCase() + " entity")
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_DESCRIPTION,
                                CodeGenerationConstants.STRING_PLACEHOLDER,
                                "POST API to create a new " + businessPurposeOfSQL.toLowerCase() + " record")
                        .build())
                .addParameter(ParameterSpec.builder(insertDtoTypeName, "insertRequest")
                        .addAnnotation(AnnotationSpec.builder(Valid.class).build())
                        .addAnnotation(AnnotationSpec.builder(RequestBody.class).build())
                        .build())
                .returns(ClassName.get("org.springframework.http", "ResponseEntity").withoutAnnotations());

        // Add user ID header parameter and DAO call based on audit configuration
        if (auditConfig.enabled()) {
            // Add @RequestHeader parameter for user ID
            methodBuilder.addParameter(ParameterSpec.builder(String.class, "userId")
                    .addAnnotation(AnnotationSpec.builder(RequestHeader.class)
                            .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_VALUE,
                                    CodeGenerationConstants.STRING_PLACEHOLDER, auditConfig.userHeaderName())
                            .addMember("required", "$L", true)
                            .build())
                    .addAnnotation(AnnotationSpec.builder(Parameter.class)
                            .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_DESCRIPTION,
                                    CodeGenerationConstants.STRING_PLACEHOLDER, "User ID for audit purposes")
                            .build())
                    .build());

            // DAO call with userId parameter
            methodBuilder.addStatement("int rowsAffected = $N.insert$L(insertRequest, userId)",
                    daoInstanceFieldName, businessPurposeOfSQL);
        } else {
            // Standard DAO call without userId
            methodBuilder.addStatement("int rowsAffected = $N.insert$L(insertRequest)",
                    daoInstanceFieldName, businessPurposeOfSQL);
        }

        // Add response handling
        methodBuilder.addCode(CodeBlock.builder()
                .beginControlFlow("if (rowsAffected > 0)")
                .addStatement("return $T.status($T.CREATED).body($S)",
                        ResponseEntity.class, HttpStatus.class, businessPurposeOfSQL.toLowerCase() + " created successfully")
                .nextControlFlow("else")
                .addStatement("return $T.status($T.INTERNAL_SERVER_ERROR).body($S)",
                        ResponseEntity.class, HttpStatus.class, "Failed to create " + businessPurposeOfSQL.toLowerCase())
                .endControlFlow()
                .build());

        MethodSpec insertMethodSpec = methodBuilder.build();

        // Controller class definition
        TypeSpec.Builder controllerBuilder = TypeSpec.classBuilder(businessPurposeOfSQL + "InsertController")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("path", "$S", "/api")
                        .build());
        TypeSpec controller = controllerBuilder
                .addAnnotation(AnnotationSpec.builder(Tag.class)
                        .addMember("name", CodeGenerationConstants.STRING_PLACEHOLDER, businessPurposeOfSQL)
                        .addMember(CodeGenerationConstants.ANNOTATION_MEMBER_DESCRIPTION,
                                CodeGenerationConstants.STRING_PLACEHOLDER,
                                businessPurposeOfSQL + " INSERT operations")
                        .build())
                .addField(FieldSpec.builder(insertDaoTypeName, daoInstanceFieldName)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(insertDaoTypeName, daoInstanceFieldName)
                        .addStatement("this.$N = $N", daoInstanceFieldName, daoInstanceFieldName)
                        .build())
                .addMethod(insertMethodSpec)
                .build();

        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "controller"), controller)
                .build();

        log.info(javaFile.toString());

        return javaFile;
    }
}
