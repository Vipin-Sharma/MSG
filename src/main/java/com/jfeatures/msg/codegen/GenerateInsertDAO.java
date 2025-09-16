package com.jfeatures.msg.codegen;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.util.CommonJavaPoetBuilders;
import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import com.jfeatures.msg.codegen.util.JavaPoetTypeNameBuilder;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.springframework.stereotype.Component;

/**
 * Generates DAO classes for INSERT operations.
 * Following Vipin's Principle: Single responsibility - INSERT DAO generation only.
 */
@Slf4j
public class GenerateInsertDAO {

    private GenerateInsertDAO() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates INSERT DAO with insert method using NamedParameterJdbcTemplate.
     * Single responsibility: Generate INSERT data access object.
     */
    public static JavaFile createInsertDAO(String businessPurposeOfSQL, InsertMetadata insertMetadata) throws IOException {
        
        if (businessPurposeOfSQL == null || businessPurposeOfSQL.trim().isEmpty()) {
            throw new IllegalArgumentException("Business purpose of SQL cannot be null or empty");
        }
        if (insertMetadata == null) {
            throw new IllegalArgumentException("Insert metadata cannot be null");
        }
        if (insertMetadata.insertColumns().isEmpty()) {
            throw new IllegalArgumentException("Insert metadata must have at least one column");
        }
        
        String jdbcTemplateFieldName = CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME;
        
        // Use CommonJavaPoetBuilders for constructor and field
        MethodSpec constructorSpec = CommonJavaPoetBuilders.jdbcTemplateConstructor(jdbcTemplateFieldName);
        FieldSpec jdbcTemplateFieldSpec = CommonJavaPoetBuilders.jdbcTemplateField(jdbcTemplateFieldName);
        
        // Generate INSERT SQL with named parameters
        String insertSql = generateInsertSql(insertMetadata);
        String formattedSql = SqlFormatter.format(insertSql);
        
        // Use CommonJavaPoetBuilders for SQL field
        FieldSpec sqlFieldSpec = CommonJavaPoetBuilders.sqlFieldWithName(formattedSql, CodeGenerationConstants.SQL_FIELD_NAME);
        
        // DTO type
        TypeName insertDtoTypeName = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessPurposeOfSQL, "dto", "InsertDTO");
        
        // INSERT method
        MethodSpec insertMethodSpec = createInsertMethod(businessPurposeOfSQL, insertMetadata, insertDtoTypeName, jdbcTemplateFieldName);
        
        // DAO class
        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL + "InsertDAO")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class)
                .addField(sqlFieldSpec)
                .addField(jdbcTemplateFieldSpec)
                .addMethod(constructorSpec)
                .addMethod(insertMethodSpec)
                .addJavadoc("Data Access Object for $L INSERT operations.\\nFollows Vipin's Principle: Single responsibility - INSERT operations only.", businessPurposeOfSQL.toLowerCase())
                .build();
        
        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "dao"), dao)
                .build();
        
        log.info(javaFile.toString());
        
        return javaFile;
    }
    
    private static String generateInsertSql(InsertMetadata insertMetadata) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(insertMetadata.tableName()).append(" (");
        
        // Column names
        List<String> columnNames = new ArrayList<>();
        for (ColumnMetadata column : insertMetadata.insertColumns()) {
            columnNames.add(column.getColumnName());
        }
        sqlBuilder.append(String.join(", ", columnNames));
        
        sqlBuilder.append(") VALUES (");
        
        // Named parameters
        List<String> parameterNames = new ArrayList<>();
        for (ColumnMetadata column : insertMetadata.insertColumns()) {
            String paramName = CaseUtils.toCamelCase(column.getColumnName(), false, '_');
            parameterNames.add(":" + paramName);
        }
        sqlBuilder.append(String.join(", ", parameterNames));
        sqlBuilder.append(")");
        
        return sqlBuilder.toString();
    }
    
    private static MethodSpec createInsertMethod(String businessPurposeOfSQL, InsertMetadata insertMetadata, 
                                               TypeName insertDtoType, String jdbcTemplateFieldName) {
        
        // Method parameter
        ParameterSpec dtoParameter = ParameterSpec.builder(insertDtoType, "insertRequest")
                .build();
        
        // Build parameter mapping code
        CodeBlock.Builder paramMappingBuilder = CodeBlock.builder()
                .addStatement("$T<$T, $T> sqlParamMap = new $T()", Map.class, String.class, Object.class, HashMap.class);
        
        for (ColumnMetadata column : insertMetadata.insertColumns()) {
            String fieldName = CaseUtils.toCamelCase(column.getColumnName(), false, '_');
            String getterMethod = "get" + CaseUtils.toCamelCase(column.getColumnName(), true, '_');
            
            paramMappingBuilder.addStatement("sqlParamMap.put($S, insertRequest.$L())", 
                    fieldName, getterMethod);
        }
        
        // Return insert method
        return MethodSpec.methodBuilder("insert" + businessPurposeOfSQL)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(dtoParameter)
                .returns(int.class)
                .addCode(paramMappingBuilder.build())
                .addStatement("return $N.update(" + CodeGenerationConstants.SQL_FIELD_NAME + ", sqlParamMap)", jdbcTemplateFieldName)
                .addJavadoc("Inserts a new $L record into the database.\\n@param insertRequest the $L data to insert\\n@return number of rows affected", 
                           businessPurposeOfSQL.toLowerCase(), businessPurposeOfSQL.toLowerCase())
                .build();
    }
}
