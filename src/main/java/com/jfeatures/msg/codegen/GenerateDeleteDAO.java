package com.jfeatures.msg.codegen;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.DeleteMetadata;
import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.jfeatures.msg.codegen.sql.SqlParameterReplacer;
import com.jfeatures.msg.codegen.SQLServerDataTypeEnum;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.text.CaseUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates DAO classes for DELETE operations.
 * Following Vipin's Principle: Single responsibility - DELETE DAO generation only.
 */
public class GenerateDeleteDAO {
    
    /**
     * Creates DELETE DAO with delete method using NamedParameterJdbcTemplate.
     * Single responsibility: Generate DELETE data access object.
     */
    public static JavaFile createDeleteDAO(String businessPurposeOfSQL, DeleteMetadata deleteMetadata) throws IOException {
        
        if (businessPurposeOfSQL == null || businessPurposeOfSQL.trim().isEmpty()) {
            throw new IllegalArgumentException("Business purpose of SQL cannot be null or empty");
        }
        if (deleteMetadata == null) {
            throw new IllegalArgumentException("Delete metadata cannot be null");
        }
        if (deleteMetadata.whereColumns().isEmpty()) {
            throw new IllegalArgumentException("Delete metadata must have at least one WHERE column");
        }
        
        String jdbcTemplateFieldName = CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME;
        
        // Constructor
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(NamedParameterJdbcTemplate.class, jdbcTemplateFieldName)
                .addStatement("this.$N = $N", jdbcTemplateFieldName, jdbcTemplateFieldName)
                .build();
        
        // JDBC template field
        FieldSpec jdbcTemplateFieldSpec = FieldSpec.builder(NamedParameterJdbcTemplate.class, jdbcTemplateFieldName, 
                Modifier.PRIVATE, Modifier.FINAL).build();
        
        // Convert original SQL to named parameter format
        String namedParameterSql = SqlParameterReplacer.convertToNamedParameterSql(
                deleteMetadata.originalSql(), 
                convertToDBColumns(deleteMetadata.whereColumns())
        );
        String formattedSql = SqlFormatter.format(namedParameterSql);
        
        // SQL field with text block
        FieldSpec sqlFieldSpec = FieldSpec.builder(String.class, CodeGenerationConstants.SQL_FIELD_NAME, 
                Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("\"\"\"\n$L\"\"\"", formattedSql)
                .addJavadoc("SQL statement for deleting $L records", businessPurposeOfSQL.toLowerCase())
                .build();
        
        // DELETE method
        MethodSpec deleteMethodSpec = createDeleteMethod(businessPurposeOfSQL, deleteMetadata, jdbcTemplateFieldName);
        
        // DAO class
        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL + "DeleteDAO")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class)
                .addField(sqlFieldSpec)
                .addField(jdbcTemplateFieldSpec)
                .addMethod(constructorSpec)
                .addMethod(deleteMethodSpec)
                .addJavadoc("Data Access Object for $L DELETE operations.\\nFollows Vipin's Principle: Single responsibility - DELETE operations only.", businessPurposeOfSQL.toLowerCase())
                .build();
        
        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "dao"), dao)
                .build();
        
        javaFile.writeTo(System.out);
        
        return javaFile;
    }
    
    private static MethodSpec createDeleteMethod(String businessPurposeOfSQL, DeleteMetadata deleteMetadata, 
                                               String jdbcTemplateFieldName) {
        
        // Method builder
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("delete" + businessPurposeOfSQL)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addJavadoc("Deletes a $L record from the database.\\n@return number of rows affected", businessPurposeOfSQL.toLowerCase());
        
        // Add parameters and build parameter mapping
        CodeBlock.Builder paramMappingBuilder = CodeBlock.builder()
                .addStatement("$T<$T, $T> sqlParamMap = new $T()", Map.class, String.class, Object.class, HashMap.class);
        
        for (ColumnMetadata column : deleteMetadata.whereColumns()) {
            Class<?> paramType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            String paramName = CaseUtils.toCamelCase(column.getColumnName(), false);
            
            // Add method parameter
            methodBuilder.addParameter(ParameterSpec.builder(paramType, paramName).build());
            
            // Add parameter mapping
            paramMappingBuilder.addStatement("sqlParamMap.put($S, $L)", paramName, paramName);
            
            // Add JavaDoc parameter
            methodBuilder.addJavadoc("@param $L the $L value for deletion criteria\\n", paramName, column.getColumnName());
        }
        
        // Complete method
        return methodBuilder
                .addCode(paramMappingBuilder.build())
                .addStatement("return $N.update(" + CodeGenerationConstants.SQL_FIELD_NAME + ", sqlParamMap)", jdbcTemplateFieldName)
                .build();
    }
    
    private static java.util.List<com.jfeatures.msg.codegen.domain.DBColumn> convertToDBColumns(java.util.List<ColumnMetadata> columnMetadataList) {
        java.util.List<com.jfeatures.msg.codegen.domain.DBColumn> dbColumns = new java.util.ArrayList<>();
        for (ColumnMetadata columnMetadata : columnMetadataList) {
            // Create DBColumn from ColumnMetadata
            com.jfeatures.msg.codegen.domain.DBColumn dbColumn = new com.jfeatures.msg.codegen.domain.DBColumn(
                    "",  // tableName - not needed for parameter conversion
                    columnMetadata.getColumnName(),
                    columnMetadata.getColumnTypeName(), // javaType
                    columnMetadata.getColumnTypeName()  // jdbcType
            );
            dbColumns.add(dbColumn);
        }
        return dbColumns;
    }
}