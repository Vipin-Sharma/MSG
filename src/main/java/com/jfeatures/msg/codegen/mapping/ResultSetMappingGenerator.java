package com.jfeatures.msg.codegen.mapping;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.jdbc.JdbcMethodSelector;
import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.apache.commons.text.CaseUtils;

import java.util.List;

public class ResultSetMappingGenerator {
    
    public static String buildResultSetToObjectMappingCode(List<ColumnMetadata> resultSetColumnDefinitions, TypeName targetDtoType) {
        if (resultSetColumnDefinitions == null) {
            throw new IllegalArgumentException("ResultSet column definitions list cannot be null");
        }
        if (resultSetColumnDefinitions.isEmpty()) {
            throw new IllegalArgumentException("ResultSet column definitions list cannot be empty");
        }
        if (targetDtoType == null) {
            throw new IllegalArgumentException("Target DTO type name cannot be null");
        }
        
        StringBuilder code = new StringBuilder();
        
        if (resultSetColumnDefinitions.size() <= CodeGenerationConstants.BUILDER_PATTERN_FIELD_THRESHOLD) {
            buildBuilderPatternMappingCode(code, resultSetColumnDefinitions, targetDtoType);
        } else {
            buildSetterPatternMappingCode(code, resultSetColumnDefinitions, targetDtoType);
        }
        
        code.append(CodeGenerationConstants.RESULT_LIST_NAME + ".add(" + CodeGenerationConstants.DTO_VARIABLE_NAME + ")");
        return code.toString();
    }
    
    private static void buildBuilderPatternMappingCode(StringBuilder code, List<ColumnMetadata> resultSetColumnDefinitions, TypeName targetDtoType) {
        TypeName builderType = getBuilderType(targetDtoType);
        code.append(((ClassName) builderType).canonicalName())
            .append(" ").append(CodeGenerationConstants.BUILDER_VARIABLE_NAME).append(" = ")
            .append(((ClassName) targetDtoType).canonicalName())
            .append(".builder();\n");
        
        code.append(((ClassName) targetDtoType).simpleName()).append(" ").append(CodeGenerationConstants.DTO_VARIABLE_NAME)
            .append(" = ").append(CodeGenerationConstants.BUILDER_VARIABLE_NAME);
        
        for (ColumnMetadata columnMetadata : resultSetColumnDefinitions) {
            String fieldName = convertColumnNameToFieldName(columnMetadata);
            String jdbcMethod = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            String columnLabel = getColumnLabel(columnMetadata);
            
            code.append(".").append(fieldName)
                .append("(rs.").append(jdbcMethod)
                .append("(\"").append(columnLabel).append("\"))")
                .append("\n");
        }
        code.append(".build();\n");
    }
    
    private static void buildSetterPatternMappingCode(StringBuilder code, List<ColumnMetadata> resultSetColumnDefinitions, TypeName targetDtoType) {
        code.append(((ClassName) targetDtoType).canonicalName())
            .append(" ").append(CodeGenerationConstants.DTO_VARIABLE_NAME).append(" = new ")
            .append(((ClassName) targetDtoType).canonicalName())
            .append("();\n");
        
        for (ColumnMetadata columnMetadata : resultSetColumnDefinitions) {
            String fieldName = convertColumnNameToFieldName(columnMetadata);
            String jdbcMethod = JdbcMethodSelector.selectJdbcGetterMethodFor(columnMetadata);
            String columnLabel = getColumnLabel(columnMetadata);
            
            code.append(CodeGenerationConstants.DTO_VARIABLE_NAME).append(".").append(CodeGenerationConstants.SETTER_METHOD_PREFIX).append(capitalize(fieldName))
                .append("(rs.").append(jdbcMethod)
                .append("(\"").append(columnLabel).append("\"));\n");
        }
    }
    
    private static TypeName getBuilderType(TypeName typeName) {
        return ClassName.get(((ClassName) typeName).packageName(), ((ClassName) typeName).simpleName(), "Builder");
    }
    
    private static String convertColumnNameToFieldName(ColumnMetadata columnMetadata) {
        String columnName = getColumnLabel(columnMetadata);
        return CaseUtils.toCamelCase(columnName, false, '_');
    }
    
    private static String getColumnLabel(ColumnMetadata columnMetadata) {
        return columnMetadata.getColumnAlias() != null ? 
            columnMetadata.getColumnAlias() : columnMetadata.getColumnName();
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}