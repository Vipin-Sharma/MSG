package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.util.DtoFieldNameConverter;
import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import javax.lang.model.element.Modifier;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates DTO classes for INSERT operations.
 * Following Vipin's Principle: Single responsibility - INSERT DTO generation only.
 */
@Slf4j
public class GenerateInsertDTO {
    
    /**
     * Creates INSERT DTO from metadata extracted from INSERT statement.
     * Single responsibility: Generate INSERT request DTO.
     */
    public static JavaFile createInsertDTO(String businessPurposeOfSQL, InsertMetadata insertMetadata) throws IOException {
        
        if (businessPurposeOfSQL == null || businessPurposeOfSQL.trim().isEmpty()) {
            throw new IllegalArgumentException("Business purpose of SQL cannot be null or empty");
        }
        if (insertMetadata == null) {
            throw new IllegalArgumentException("Insert metadata cannot be null");
        }
        if (insertMetadata.insertColumns().isEmpty()) {
            throw new IllegalArgumentException("Insert metadata must have at least one column");
        }
        
        TypeSpec.Builder insertDTOBuilder = TypeSpec.classBuilder(businessPurposeOfSQL + "InsertDTO")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Builder.class)
                        .addMember("builderClassName", "$S", "Builder")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Value.class).build())
                .addAnnotation(AnnotationSpec.builder(Jacksonized.class).build())
                .addJavadoc("DTO for creating new $L entities via POST API.\nContains all fields required for insertion.", businessPurposeOfSQL.toLowerCase());
        
        // Generate fields for all INSERT columns
        for (ColumnMetadata column : insertMetadata.insertColumns()) {
            Class<?> fieldType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            String fieldName = DtoFieldNameConverter.convertToJavaCamelCase(column.getColumnName());
            
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, fieldName)
                    .addModifiers(Modifier.PRIVATE);
            
            // Add validation annotations based on column metadata
            if (column.getIsNullable() == 0) { // NOT NULL
                fieldBuilder.addAnnotation(AnnotationSpec.builder(jakarta.validation.constraints.NotNull.class)
                        .addMember("message", "$S", fieldName + " is required for " + businessPurposeOfSQL.toLowerCase() + " creation")
                        .build());
            }
            
            // Add documentation
            fieldBuilder.addJavadoc("$L field for $L insertion", 
                    fieldName, businessPurposeOfSQL.toLowerCase());
            
            insertDTOBuilder.addField(fieldBuilder.build());
        }
        
        TypeSpec insertDTO = insertDTOBuilder.build();
        
        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "dto"), insertDTO)
                .build();
        
        log.info(javaFile.toString());

        return javaFile;
    }
}