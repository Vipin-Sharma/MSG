package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.DeleteMetadata;
import com.squareup.javapoet.*;
import jakarta.validation.constraints.NotNull;
import javax.lang.model.element.Modifier;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.text.CaseUtils;

/**
 * Generates DTO classes for DELETE operations with WHERE clause parameters.
 * Following Vipin's Principle: Single responsibility - DELETE DTO generation only.
 */
public class GenerateDeleteDTO {
    
    /**
     * Creates a DTO class for DELETE operations.
     * Single responsibility: Generate DELETE DTO with WHERE parameters.
     */
    public static JavaFile createDeleteDTO(String businessDomainName, DeleteMetadata deleteMetadata) {
        String className = businessDomainName + "DeleteDTO";
        String packageName = "com.jfeatures.msg." + businessDomainName + ".dto";
        
        // Build the DTO class
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Builder.class)
                .addAnnotation(Value.class)
                .addAnnotation(Jacksonized.class)
                .addJavadoc("DTO for DELETE operations on $L table with WHERE clause parameters.", deleteMetadata.tableName());
        
        // Generate fields for WHERE clause parameters
        for (ColumnMetadata column : deleteMetadata.whereColumns()) {
            Class<?> fieldType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            String fieldName = CaseUtils.toCamelCase(column.getColumnName(), false);
            
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, fieldName)
                    .addModifiers(Modifier.PRIVATE);
            
            // Add validation annotation for non-nullable fields (assuming WHERE parameters are required)
            if (column.getIsNullable() == 0) {
                fieldBuilder.addAnnotation(AnnotationSpec.builder(NotNull.class)
                        .addMember("message", "$S", fieldName + " is required for " + businessDomainName.toLowerCase() + " deletion")
                        .build());
            }
            
            fieldBuilder.addJavadoc("WHERE parameter: $L", column.getColumnName());
            classBuilder.addField(fieldBuilder.build());
        }
        
        // Build the complete class
        TypeSpec dtoClass = classBuilder.build();
        
        return JavaFile.builder(packageName, dtoClass).build();
    }
}