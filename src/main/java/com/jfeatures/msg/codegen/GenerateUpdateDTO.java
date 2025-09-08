package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import com.jfeatures.msg.codegen.util.DtoFieldNameConverter;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.Modifier;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates DTO classes for UPDATE statements.
 * Creates separate DTOs for the request body (SET columns) and path/query parameters (WHERE columns).
 */
@Slf4j
public class GenerateUpdateDTO {
    
    /**
     * Creates the main update DTO containing fields for SET clause columns.
     */
    public static JavaFile createUpdateDTO(String businessPurposeOfSQL, UpdateMetadata updateMetadata) throws IOException {
        List<FieldSpec> fieldSpecList = generateFieldSpecsForSetColumns(updateMetadata.setColumns());
        
        TypeSpec updateDTO = TypeSpec.classBuilder(businessPurposeOfSQL + "UpdateDTO")
                .addModifiers(Modifier.PUBLIC)
                .addFields(fieldSpecList)
                .addAnnotation(AnnotationSpec.builder(Data.class).build())
                .addJavadoc("DTO for updating $L entity.\nContains fields that can be updated via PUT API.", businessPurposeOfSQL.toLowerCase())
                .build();
        
        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "dto"), updateDTO)
                .build();
        
        log.info(javaFile.toString());
        return javaFile;
    }
    
    /**
     * Creates a separate DTO for WHERE clause parameters if needed.
     * This is useful for complex WHERE clauses with multiple parameters.
     */
    public static JavaFile createUpdateWhereDTO(String businessPurposeOfSQL, UpdateMetadata updateMetadata) throws IOException {
        if (updateMetadata.whereColumns().isEmpty()) {
            return null; // No WHERE DTO needed if no WHERE parameters
        }
        
        List<FieldSpec> fieldSpecList = generateFieldSpecsForWhereColumns(updateMetadata.whereColumns());
        
        TypeSpec whereDTO = TypeSpec.classBuilder(businessPurposeOfSQL + "UpdateWhereDTO")
                .addModifiers(Modifier.PUBLIC)
                .addFields(fieldSpecList)
                .addAnnotation(AnnotationSpec.builder(Data.class).build())
                .addJavadoc("DTO for WHERE clause parameters in $L update operations.", businessPurposeOfSQL.toLowerCase())
                .build();
        
        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "dto"), whereDTO)
                .build();
        
        log.info(javaFile.toString());
        return javaFile;
    }
    
    /**
     * Generates field specifications for SET columns (fields to be updated).
     */
    private static List<FieldSpec> generateFieldSpecsForSetColumns(List<ColumnMetadata> setColumns) {
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        
        for (ColumnMetadata column : setColumns) {
            Class<?> fieldType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            String fieldName = DtoFieldNameConverter.convertToJavaCamelCase(column.getColumnName());
            
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, fieldName)
                    .addModifiers(Modifier.PRIVATE);
            
            // Add @NotNull annotation for non-nullable columns
            if (column.getIsNullable() == 0) {
                fieldBuilder.addAnnotation(AnnotationSpec.builder(NotNull.class)
                        .addMember("message", "$S", fieldName + " cannot be null")
                        .build());
            }
            
            // Add JavaDoc
            fieldBuilder.addJavadoc("The $L to update", fieldName.replaceAll("([A-Z])", " $1").toLowerCase().trim());
            
            fieldSpecList.add(fieldBuilder.build());
        }
        
        return fieldSpecList;
    }
    
    /**
     * Generates field specifications for WHERE columns (filter parameters).
     */
    private static List<FieldSpec> generateFieldSpecsForWhereColumns(List<ColumnMetadata> whereColumns) {
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        
        for (int i = 0; i < whereColumns.size(); i++) {
            ColumnMetadata column = whereColumns.get(i);
            Class<?> fieldType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            
            // Generate meaningful field names for WHERE parameters
            String fieldName = generateWhereFieldName(column, i);
            
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, fieldName)
                    .addModifiers(Modifier.PRIVATE);
            
            // WHERE parameters are typically required for updates
            fieldBuilder.addAnnotation(AnnotationSpec.builder(NotNull.class)
                    .addMember("message", "$S", fieldName + " cannot be null")
                    .build());
            
            // Add JavaDoc
            fieldBuilder.addJavadoc("WHERE clause parameter: $L", fieldName);
            
            fieldSpecList.add(fieldBuilder.build());
        }
        
        return fieldSpecList;
    }
    
    /**
     * Generates meaningful field names for WHERE clause parameters.
     */
    private static String generateWhereFieldName(ColumnMetadata column, int index) {
        String columnName = column.getColumnName();
        
        // If column name is meaningful (not generic like "whereParam1"), use it
        if (!columnName.startsWith("whereParam")) {
            return DtoFieldNameConverter.convertToJavaCamelCase(columnName);
        }
        
        // Generate names based on common WHERE clause patterns
        return switch (index) {
            case 0 -> "id";
            case 1 -> "status";
            case 2 -> "category";
            default -> "param" + (index + 1);
        };
    }
}