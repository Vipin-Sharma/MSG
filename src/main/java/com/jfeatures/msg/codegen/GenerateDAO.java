package com.jfeatures.msg.codegen;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.domain.TableColumn;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.codegen.util.TypeUtil;
import com.jfeatures.msg.sql.ModifySQL;
import com.jfeatures.msg.sql.MsgSqlParser;
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
    public static JavaFile createDao(String businessPurposeOfSQL, List<DBColumn> predicateHavingLiterals, String sql, Map<String, String> ddlPerTableName) throws Exception {

        String jdbcTemplateInstanceFieldName = "namedParameterJdbcTemplate";


        CodeBlock codeblock = CodeBlock.builder()
                .addStatement("this.$N = $N", jdbcTemplateInstanceFieldName, jdbcTemplateInstanceFieldName) //not working correctly
                .build();
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName)
                .addCode(codeblock)
                .build();


        FieldSpec jdbcTemplateFieldSpec = FieldSpec.builder(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName, Modifier.PRIVATE, Modifier.FINAL).build();

        String modifiedSQL = ModifySQL.modifySQLToUseNamedParameter(sql);
        String formattedSQL = SqlFormatter.format(modifiedSQL);
        formattedSQL = formattedSQL.replace(": ", ":");
        log.info(formattedSQL);
        FieldSpec sqlFieldSpec = FieldSpec.builder(String.class, "SQL", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", formattedSQL)
                .build();

        TypeName dtoTypeName = TypeUtil.getJavaClassTypeName(businessPurposeOfSQL, "dto", "DTO");

        ArrayList<ParameterSpec> parameters = new ArrayList<>();

        predicateHavingLiterals.forEach(literal ->
                parameters.add(ParameterSpec.builder(ClassName.bestGuess(literal.javaType()).box(),
                        CaseUtils.toCamelCase(literal.columnName(), false)
                ).build()));

        CodeBlock codeBlockForSqlParamsMap = CodeBlock.builder()
                .addStatement("$T<String, Object> sqlParamMap = new $T()", Map.class, HashMap.class)
                .build();

        Map<TableColumn, DBColumn> selectColumnDetailsToGetDataFromResultSet = MsgSqlParser.getDetailsOfColumnsUsedInSelect(sql, ddlPerTableName);
        String codeToSetColumnValuesFromResultSet = "";

        if (selectColumnDetailsToGetDataFromResultSet.size() <= 255) {
            TypeName builderTypeForDto = getBuilderType(dtoTypeName);
            codeToSetColumnValuesFromResultSet = codeToSetColumnValuesFromResultSet + ((ClassName) builderTypeForDto).canonicalName() + " "
                    + ((ClassName) builderTypeForDto).simpleName() + " = " + ((ClassName) dtoTypeName).canonicalName() + ".builder();\n";
            codeToSetColumnValuesFromResultSet = codeToSetColumnValuesFromResultSet + ((ClassName) dtoTypeName).simpleName() + " dto = Builder.";
            for (Map.Entry<TableColumn, DBColumn> entry : selectColumnDetailsToGetDataFromResultSet.entrySet()) {
                TableColumn tableColumn = entry.getKey();
                DBColumn dbColumn = entry.getValue();
                String fieldName = NameUtil.getFieldNameForDTO(tableColumn);
                codeToSetColumnValuesFromResultSet = codeToSetColumnValuesFromResultSet + fieldName
                        + "(rs.get" +
                        dbColumn.jdbcType()
                        + "(\""
                        + (tableColumn.columnAliasIfAvailable() != null ? tableColumn.columnAliasIfAvailable() : tableColumn.columnName())
                        + "\"))."
                        + "\n";
            }

            codeToSetColumnValuesFromResultSet += "build();\n";
        }
        else {
            codeToSetColumnValuesFromResultSet = codeToSetColumnValuesFromResultSet + ((ClassName) dtoTypeName).canonicalName() + " dto = new " + ((ClassName) dtoTypeName).canonicalName() + "();\n";

            for (Map.Entry<TableColumn, DBColumn> entry : selectColumnDetailsToGetDataFromResultSet.entrySet()) {
                TableColumn tableColumn = entry.getKey();
                DBColumn dbColumn = entry.getValue();
                String fieldName = NameUtil.getFieldNameForDTO(tableColumn);
                codeToSetColumnValuesFromResultSet = codeToSetColumnValuesFromResultSet + "dto.set"
                        + fieldName
                        + "(rs.get" +
                        dbColumn.jdbcType()
                        + "(\""
                        + (tableColumn.columnAliasIfAvailable() != null ? tableColumn.columnAliasIfAvailable() : tableColumn.columnName())
                        + "\"));"
                        + "\n";
            }

        }
        codeToSetColumnValuesFromResultSet += "result.add(dto)";

        TypeSpec rowCallbackHandler = TypeSpec
                .anonymousClassBuilder("")
                .addSuperinterface(RowCallbackHandler.class)
                .addMethod(MethodSpec.methodBuilder("processRow")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ResultSet.class, "rs")
                        .addException(SQLException.class)
                        .addStatement(codeToSetColumnValuesFromResultSet)
                        .build())
                .build();

        CodeBlock.Builder codeBlockForJdbcQuery = CodeBlock.builder();
        codeBlockForJdbcQuery.add(jdbcTemplateInstanceFieldName + ".query(SQL, sqlParamMap, " + rowCallbackHandler + ");\n");

        ClassName list = ClassName.get("java.util", "List");
        ParameterizedTypeName parameterizedTypeName = TypeUtil.getParameterizedTypeName(dtoTypeName, list);

        //todo $L can be taken from parameter list, but current code works for a poc
        CodeBlock.Builder codeBlockHavingPredicatesMapBuilder = CodeBlock.builder();
        predicateHavingLiterals.forEach(literal -> codeBlockHavingPredicatesMapBuilder.addStatement("sqlParamMap.put($S, $L)",
                CaseUtils.toCamelCase(literal.columnName(), false),
                CaseUtils.toCamelCase(literal.columnName(), false)));
        CodeBlock codeBlockHavingPredicatesMap = codeBlockHavingPredicatesMapBuilder.build();

        MethodSpec methodSpec = MethodSpec.methodBuilder("get" + businessPurposeOfSQL)
                .addStatement("$T result = new $T()", parameterizedTypeName, ArrayList.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameters)
                .returns(parameterizedTypeName)
                .addCode(codeBlockForSqlParamsMap)
                .addCode(codeBlockHavingPredicatesMap)
                .addCode(codeBlockForJdbcQuery.build())
                .addStatement("return result")
                .build();

        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL + "DAO")
                .addModifiers(Modifier.PUBLIC)
                .addField(jdbcTemplateFieldSpec)
                .addField(sqlFieldSpec)
                .addAnnotation(Component.class)
                .addMethod(methodSpec)
                .addMethod(constructorSpec)
                .build();

        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "dao"), dao)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

    private static TypeName getBuilderType(TypeName typeName) {
        return ClassName.get(((ClassName) typeName).packageName(), ((ClassName) typeName).simpleName(), "Builder");
    }
}
