package com.jfeatures.msg.codegen;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.codegen.util.TypeUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates DAO classes for UPDATE operations using database metadata.
 */
@Slf4j
public class GenerateUpdateDAO {
    
    /**
     * Creates DAO with UPDATE method using database metadata approach.
     * Follows clean code principles with single responsibility - one public method per class.
     */
    public static JavaFile createUpdateDAO(String businessPurposeOfSQL, UpdateMetadata updateMetadata) throws Exception {
        
        String jdbcTemplateInstanceFieldName = "namedParameterJdbcTemplate";
        
        // Generate SQL constant using text block for better readability
        String namedParameterSql = generateNamedParameterSql(updateMetadata);
        FieldSpec sqlConstant = FieldSpec.builder(String.class, "SQL")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"\"\"\n$L\"\"\"", SqlFormatter.format(namedParameterSql))
                .addJavadoc("SQL statement for updating $L records", businessPurposeOfSQL.toLowerCase())
                .build();
        
        // Constructor
        CodeBlock constructorCodeBlock = CodeBlock.builder()
                .addStatement("this.$N = $N", jdbcTemplateInstanceFieldName, jdbcTemplateInstanceFieldName)
                .build();
        
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName)
                .addCode(constructorCodeBlock)
                .build();
        
        // Generate UPDATE method (single public method following clean code principles)
        MethodSpec updateMethod = createUpdateMethod(businessPurposeOfSQL, updateMetadata, jdbcTemplateInstanceFieldName);
        
        // Create DAO class
        TypeSpec daoClass = TypeSpec.classBuilder(businessPurposeOfSQL + "UpdateDAO")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class)
                .addAnnotation(Slf4j.class)
                .addField(FieldSpec.builder(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addField(sqlConstant)
                .addMethod(constructorSpec)
                .addMethod(updateMethod)
                .build();
        
        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "dao"), daoClass)
                .build();
        
        javaFile.writeTo(System.out);
        return javaFile;
    }
    
    /**
     * Creates the main UPDATE method.
     */
    private static MethodSpec createUpdateMethod(String businessPurposeOfSQL, UpdateMetadata updateMetadata, String jdbcTemplateFieldName) {
        
        TypeName updateDtoType = TypeUtil.getJavaClassTypeName(businessPurposeOfSQL, "dto", "UpdateDTO");
        
        // Build parameter specifications
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        parameterSpecs.add(ParameterSpec.builder(updateDtoType, "updateDto")
                .addAnnotation(ClassName.get("jakarta.validation", "Valid"))
                .build());
        
        // Add WHERE parameters
        List<String> whereParamNames = new ArrayList<>();
        for (int i = 0; i < updateMetadata.whereColumns().size(); i++) {
            ColumnMetadata column = updateMetadata.whereColumns().get(i);
            Class<?> paramType = SQLServerDataTypeEnum.getClassForType(column.getColumnTypeName());
            String paramName = generateWhereParamName(column, i);
            whereParamNames.add(paramName);
            
            parameterSpecs.add(ParameterSpec.builder(paramType, paramName).build());
        }
        
        // Build parameter map construction code
        CodeBlock paramMapCode = buildParameterMapCode(updateMetadata, whereParamNames);
        
        // Build method body using SQL constant (clean code practice)
        CodeBlock methodBody = CodeBlock.builder()
                .addStatement("$T<String, Object> paramMap = new $T<>()", Map.class, HashMap.class)
                .add(paramMapCode)
                .add("\n")
                .addStatement("log.info(\"Executing UPDATE: {}\", SQL)")
                .addStatement("log.debug(\"Parameters: {}\", paramMap)")
                .add("\n")
                .addStatement("int rowsUpdated = $N.update(SQL, paramMap)", jdbcTemplateFieldName)
                .addStatement("log.info(\"Updated {} rows for {}\", rowsUpdated, $S)", businessPurposeOfSQL)
                .addStatement("return rowsUpdated")
                .build();
        
        return MethodSpec.methodBuilder("update" + businessPurposeOfSQL)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecs)
                .returns(int.class)
                .addCode(methodBody)
                .addJavadoc("Updates $L record(s) in the database.\n@param updateDto The data to update\n@return Number of rows updated", businessPurposeOfSQL.toLowerCase())
                .build();
    }
    
    /**
     * Generates SQL with named parameters instead of positional parameters.
     */
    private static String generateNamedParameterSql(UpdateMetadata updateMetadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(updateMetadata.tableName()).append(" SET ");
        
        // Build SET clause with named parameters
        List<String> setClauses = new ArrayList<>();
        for (ColumnMetadata column : updateMetadata.setColumns()) {
            String paramName = CaseUtils.toCamelCase(column.getColumnName(), false, '_');
            setClauses.add(column.getColumnName() + " = :" + paramName);
        }
        sql.append(String.join(", ", setClauses));
        
        // Add WHERE clause if present
        if (!updateMetadata.whereColumns().isEmpty()) {
            sql.append(" WHERE ");
            List<String> whereClauses = new ArrayList<>();
            for (int i = 0; i < updateMetadata.whereColumns().size(); i++) {
                ColumnMetadata column = updateMetadata.whereColumns().get(i);
                String paramName = generateWhereParamName(column, i);
                // For WHERE clauses, we need to guess the column names from the original SQL
                // This is a simplification - in practice, we'd need better parsing
                String columnName = i == 0 ? "id" : "param" + (i + 1);
                whereClauses.add(columnName + " = :" + paramName);
            }
            sql.append(String.join(" AND ", whereClauses));
        }
        
        return sql.toString();
    }
    
    /**
     * Builds the code block for constructing the parameter map.
     */
    private static CodeBlock buildParameterMapCode(UpdateMetadata updateMetadata, List<String> whereParamNames) {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        
        // Add SET parameters from DTO
        for (ColumnMetadata column : updateMetadata.setColumns()) {
            String paramName = CaseUtils.toCamelCase(column.getColumnName(), false, '_');
            String getterMethod = "get" + CaseUtils.toCamelCase(column.getColumnName(), true, '_');
            codeBuilder.addStatement("paramMap.put($S, updateDto.$N())", paramName, getterMethod);
        }
        
        // Add WHERE parameters
        for (int i = 0; i < whereParamNames.size(); i++) {
            String paramName = whereParamNames.get(i);
            codeBuilder.addStatement("paramMap.put($S, $N)", paramName, paramName);
        }
        
        return codeBuilder.build();
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