package com.jfeatures.msg.codegen;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.codegen.util.TypeUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class GenerateDAO {
    private static TypeName getBuilderType(TypeName typeName) {
        return ClassName.get(((ClassName) typeName).packageName(), ((ClassName) typeName).simpleName(), "Builder");
    }
    
    /**
     * Creates DAO using database metadata instead of complex SQL parsing.
     * Much simpler, more reliable, and easier to maintain.
     */
    public static JavaFile createDaoFromMetadata(String businessPurposeOfSQL, 
                                                List<ColumnMetadata> selectColumnMetadata, 
                                                List<DBColumn> predicateHavingLiterals, 
                                                String sql) throws Exception {
        
        String jdbcTemplateInstanceFieldName = "namedParameterJdbcTemplate";
        
        // Constructor
        CodeBlock constructorCodeBlock = CodeBlock.builder()
                .addStatement("this.$N = $N", jdbcTemplateInstanceFieldName, jdbcTemplateInstanceFieldName)
                .build();
                
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName)
                .addCode(constructorCodeBlock)
                .build();
        
        // JDBC template field
        FieldSpec jdbcTemplateFieldSpec = FieldSpec.builder(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName, 
                Modifier.PRIVATE, Modifier.FINAL).build();
        
        // SQL field with named parameters - simple replacement approach
        String modifiedSQL = replaceParametersWithNamedParameters(sql, predicateHavingLiterals);
        String formattedSQL = SqlFormatter.format(modifiedSQL);
        formattedSQL = formattedSQL.replace(": ", ":");
        log.info("Generated SQL for DAO: {}", formattedSQL);
        
        FieldSpec sqlFieldSpec = FieldSpec.builder(String.class, "SQL", 
                Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", formattedSQL)
                .build();
        
        // DTO type
        TypeName dtoTypeName = TypeUtil.getJavaClassTypeName(businessPurposeOfSQL, "dto", "DTO");
        
        // Method parameters from predicateHavingLiterals
        ArrayList<ParameterSpec> parameters = new ArrayList<>();
        predicateHavingLiterals.forEach(literal ->
                parameters.add(ParameterSpec.builder(ClassName.bestGuess(literal.javaType()).box(),
                        CaseUtils.toCamelCase(literal.columnName(), false)).build()));
        
        // SQL parameters map setup
        CodeBlock sqlParamsMapCodeBlock = CodeBlock.builder()
                .addStatement("$T<String, Object> sqlParamMap = new $T()", Map.class, HashMap.class)
                .build();
        
        // Add parameters to map
        CodeBlock.Builder sqlParamMapBuilder = CodeBlock.builder();
        predicateHavingLiterals.forEach(literal -> 
                sqlParamMapBuilder.addStatement("sqlParamMap.put($S, $L)",
                        CaseUtils.toCamelCase(literal.columnName(), false),
                        CaseUtils.toCamelCase(literal.columnName(), false)));
        CodeBlock sqlParamMappingCodeBlock = sqlParamMapBuilder.build();
        
        // Generate ResultSet mapping code using ColumnMetadata
        String resultSetMappingCode = generateResultSetMappingFromMetadata(selectColumnMetadata, dtoTypeName);
        
        // Row callback handler
        TypeSpec rowCallbackHandler = TypeSpec
                .anonymousClassBuilder("")
                .addSuperinterface(RowCallbackHandler.class)
                .addMethod(MethodSpec.methodBuilder("processRow")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ResultSet.class, "rs")
                        .addException(SQLException.class)
                        .addStatement(resultSetMappingCode)
                        .build())
                .build();
        
        // JDBC query code block
        CodeBlock jdbcQueryCodeBlock = CodeBlock.builder()
                .add("$N.query(SQL, sqlParamMap, $L);\n", jdbcTemplateInstanceFieldName, rowCallbackHandler)
                .build();
        
        // Return type
        ClassName list = ClassName.get("java.util", "List");
        ParameterizedTypeName returnTypeName = TypeUtil.getParameterizedTypeName(dtoTypeName, list);
        
        // Main DAO method
        MethodSpec daoMethodSpec = MethodSpec.methodBuilder("get" + businessPurposeOfSQL)
                .addStatement("$T result = new $T()", returnTypeName, ArrayList.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameters)
                .returns(returnTypeName)
                .addCode(sqlParamsMapCodeBlock)
                .addCode(sqlParamMappingCodeBlock)
                .addCode(jdbcQueryCodeBlock)
                .addStatement("return result")
                .build();
        
        // DAO class
        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL + "DAO")
                .addModifiers(Modifier.PUBLIC)
                .addField(jdbcTemplateFieldSpec)
                .addField(sqlFieldSpec)
                .addAnnotation(Component.class)
                .addMethod(daoMethodSpec)
                .addMethod(constructorSpec)
                .build();
        
        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "dao"), dao)
                .build();
        
        javaFile.writeTo(System.out);
        
        return javaFile;
    }
    
    /**
     * Generates ResultSet mapping code directly from ColumnMetadata.
     * Much simpler than complex SQL parsing approach.
     */
    private static String generateResultSetMappingFromMetadata(List<ColumnMetadata> selectColumnMetadata, TypeName dtoTypeName) {
        StringBuilder code = new StringBuilder();
        
        if (selectColumnMetadata.size() <= 255) {
            // Use builder pattern for DTOs with <= 255 fields
            TypeName builderType = getBuilderType(dtoTypeName);
            code.append(((ClassName) builderType).canonicalName())
                .append(" Builder = ")
                .append(((ClassName) dtoTypeName).canonicalName())
                .append(".builder();\n");
            
            code.append(((ClassName) dtoTypeName).simpleName()).append(" dto = Builder");
            
            for (ColumnMetadata columnMetadata : selectColumnMetadata) {
                String fieldName = convertColumnNameToFieldName(columnMetadata);
                String jdbcMethod = getJdbcGetterMethod(columnMetadata);
                String columnLabel = columnMetadata.getColumnAlias() != null ? 
                    columnMetadata.getColumnAlias() : columnMetadata.getColumnName();
                
                code.append(".").append(fieldName)
                    .append("(rs.").append(jdbcMethod)
                    .append("(\"").append(columnLabel).append("\"))")
                    .append("\n");
            }
            code.append(".build();\n");
        } else {
            // Use setter pattern for DTOs with > 255 fields
            code.append(((ClassName) dtoTypeName).canonicalName())
                .append(" dto = new ")
                .append(((ClassName) dtoTypeName).canonicalName())
                .append("();\n");
            
            for (ColumnMetadata columnMetadata : selectColumnMetadata) {
                String fieldName = convertColumnNameToFieldName(columnMetadata);
                String jdbcMethod = getJdbcGetterMethod(columnMetadata);
                String columnLabel = columnMetadata.getColumnAlias() != null ? 
                    columnMetadata.getColumnAlias() : columnMetadata.getColumnName();
                
                code.append("dto.set").append(capitalize(fieldName))
                    .append("(rs.").append(jdbcMethod)
                    .append("(\"").append(columnLabel).append("\"));\n");
            }
        }
        
        code.append("result.add(dto)");
        return code.toString();
    }
    
    /**
     * Convert column name to field name (handles snake_case to camelCase conversion)
     */
    private static String convertColumnNameToFieldName(ColumnMetadata columnMetadata) {
        String columnName = columnMetadata.getColumnAlias() != null ? 
            columnMetadata.getColumnAlias() : columnMetadata.getColumnName();
        return CaseUtils.toCamelCase(columnName, false, '_');
    }
    
    /**
     * Get appropriate JDBC getter method based on column metadata
     */
    private static String getJdbcGetterMethod(ColumnMetadata columnMetadata) {
        return switch (columnMetadata.getColumnTypeName().toLowerCase()) {
            case "varchar", "char", "nvarchar", "nchar", "text", "ntext" -> "getString";
            case "int", "integer" -> "getInt";
            case "bigint" -> "getLong";
            case "smallint", "tinyint" -> "getShort";
            case "decimal", "numeric", "money", "smallmoney" -> "getBigDecimal";
            case "float", "real" -> "getFloat";
            case "double" -> "getDouble";
            case "bit" -> "getBoolean";
            case "datetime", "datetime2", "smalldatetime" -> "getTimestamp";
            case "date" -> "getDate";
            case "time" -> "getTime";
            case "binary", "varbinary", "image" -> "getBytes";
            default -> "getString"; // Default fallback
        };
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Simple parameter replacement without complex SQL parsing.
     * Replaces ? parameters with :paramName based on parameter order.
     */
    private static String replaceParametersWithNamedParameters(String sql, List<DBColumn> parameters) {
        String result = sql;
        
        // Replace each ? with corresponding named parameter
        for (int i = 0; i < parameters.size(); i++) {
            String paramName = CaseUtils.toCamelCase(parameters.get(i).columnName(), false);
            // Find first occurrence of ? and replace it
            result = result.replaceFirst("\\?", ":" + paramName);
        }
        
        return result;
    }
}
