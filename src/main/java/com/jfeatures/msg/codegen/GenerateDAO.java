package com.jfeatures.msg.codegen;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.mapping.ResultSetMappingGenerator;
import com.jfeatures.msg.codegen.sql.SqlParameterReplacer;
import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import com.jfeatures.msg.codegen.util.JavaPoetTypeNameBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
public class GenerateDAO {

    private GenerateDAO() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates DAO using database metadata instead of complex SQL parsing.
     * Much simpler, more reliable, and easier to maintain.
     */
    public static JavaFile createDaoFromMetadata(String businessPurposeOfSQL, 
                                                List<ColumnMetadata> selectColumnMetadata, 
                                                List<DBColumn> predicateHavingLiterals, 
                                                String sql) {
        
        if (businessPurposeOfSQL == null || businessPurposeOfSQL.trim().isEmpty()) {
            throw new IllegalArgumentException("Business purpose of SQL cannot be null or empty");
        }
        if (selectColumnMetadata == null) {
            throw new IllegalArgumentException("Select column metadata cannot be null");
        }
        if (selectColumnMetadata.isEmpty()) {
            throw new IllegalArgumentException("Select column metadata cannot be empty");
        }
        if (predicateHavingLiterals == null) {
            throw new IllegalArgumentException("Predicate having literals cannot be null");
        }
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }
        
        String jdbcTemplateInstanceFieldName = CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME;
        
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
        String modifiedSQL = SqlParameterReplacer.convertToNamedParameterSql(sql, predicateHavingLiterals);
        String formattedSQL = SqlFormatter.format(modifiedSQL);
        formattedSQL = formattedSQL.replace(": ", ":");
        log.info("Generated SQL for DAO: {}", formattedSQL);
        
        // Use text block for SQL formatting
        FieldSpec sqlFieldSpec = FieldSpec.builder(String.class, CodeGenerationConstants.SQL_FIELD_NAME, 
                Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("\"\"\"\n$L\"\"\"", formattedSQL)
                .build();
        
        // DTO type
        TypeName dtoTypeName = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessPurposeOfSQL, "dto", "DTO");
        
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
        String resultSetMappingCode = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(selectColumnMetadata, dtoTypeName);
        
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
                .add("$N.query(" + CodeGenerationConstants.SQL_FIELD_NAME + ", sqlParamMap, $L);\n", jdbcTemplateInstanceFieldName, rowCallbackHandler)
                .build();
        
        // Return type
        ClassName list = ClassName.get("java.util", "List");
        ParameterizedTypeName returnTypeName = JavaPoetTypeNameBuilder.buildParameterizedTypeName(dtoTypeName, list);
        
        // Main DAO method
        MethodSpec daoMethodSpec = MethodSpec.methodBuilder(CodeGenerationConstants.DAO_METHOD_PREFIX + businessPurposeOfSQL)
                .addStatement("$T " + CodeGenerationConstants.RESULT_LIST_NAME + " = new $T()", returnTypeName, ArrayList.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameters)
                .returns(returnTypeName)
                .addCode(sqlParamsMapCodeBlock)
                .addCode(sqlParamMappingCodeBlock)
                .addCode(jdbcQueryCodeBlock)
                .addStatement("return " + CodeGenerationConstants.RESULT_LIST_NAME)
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
        
        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "dao"), dao)
                .build();

        log.info(javaFile.toString());

        return javaFile;
    }
}
