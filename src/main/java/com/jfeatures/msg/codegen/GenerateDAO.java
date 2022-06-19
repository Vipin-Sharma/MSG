package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.domain.TableColumn;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.codegen.util.TypeUtil;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.squareup.javapoet.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import org.apache.commons.text.CaseUtils;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateDAO {
    public static JavaFile createDao(String businessPurposeOfSQL, List<DBColumn> predicateHavingLiterals, String sql, Map<String, String> ddlPerTableName) throws IOException, JSQLParserException {

        String jdbcTemplateInstanceFieldName = "namedParameterJdbcTemplate";


        CodeBlock codeblock = CodeBlock.builder()
                .addStatement("this.$N = $N", jdbcTemplateInstanceFieldName, jdbcTemplateInstanceFieldName) //not working correctly
                .build();
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName)
                .addCode(codeblock)
                .build();


        FieldSpec jdbcTemplateFieldSpec = FieldSpec.builder(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName, Modifier.PRIVATE, Modifier.FINAL).build();

        String modifiedSQL = MsgSqlParser.modifySQLToUseNamedParameter(sql);
        FieldSpec sqlFieldSpec = FieldSpec.builder(String.class, "SQL", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", modifiedSQL)
                .build();

        TypeName dtoTypeName = TypeUtil.getJavaClassTypeName(businessPurposeOfSQL, "dto", "DTO");

        ArrayList<ParameterSpec> parameters = new ArrayList<>();

        predicateHavingLiterals.forEach(literal ->
                parameters.add(ParameterSpec.builder(ClassName.bestGuess(literal.javaType()).box(),
                        literal.tableName() + CaseUtils.toCamelCase(literal.columnName(), true)
                ).build()));

        CodeBlock codeBlockForSqlParamsMap = CodeBlock.builder()
                .addStatement("$T<String, Object> sqlParamMap = new $T()", Map.class, HashMap.class)
                .build();

        Map<TableColumn, DBColumn> selectColumnDetails = MsgSqlParser.getDetailsOfColumnsUsedInSelect(sql, ddlPerTableName);

        String codeToSetColumnValuesFromResultSet = ((ClassName) dtoTypeName).simpleName() + " dto = Builder.";
        for (Map.Entry<TableColumn, DBColumn> entry : selectColumnDetails.entrySet())
        {
            TableColumn tableColumn = entry.getKey();
            DBColumn dbColumn = entry.getValue();
            codeToSetColumnValuesFromResultSet = codeToSetColumnValuesFromResultSet.concat(
                    tableColumn.tableName() + CaseUtils.toCamelCase(tableColumn.columnName(), true)
                    + "(rs.get" +
                            dbColumn.jdbcType()
                    + "(\""
                    + (tableColumn.columnAliasIfAvailable() != null ? tableColumn.columnAliasIfAvailable() : tableColumn.columnName())
                    + "\"))."
                    + "\n"
            );
        }
        codeToSetColumnValuesFromResultSet = codeToSetColumnValuesFromResultSet.concat("build();");
        codeToSetColumnValuesFromResultSet = codeToSetColumnValuesFromResultSet.concat("result.add(dto)");

        TypeName builderTypeForDto = getBuilderType(dtoTypeName);
        TypeSpec rowCallbackHandler = TypeSpec
                .anonymousClassBuilder("")
                .addSuperinterface(RowCallbackHandler.class)
                .addMethod(MethodSpec.methodBuilder("processRow")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ResultSet.class, "rs")
                        .addException(SQLException.class)
                        .addStatement("$T " + ((ClassName) builderTypeForDto).simpleName() + " = $T.builder()", builderTypeForDto, dtoTypeName)
                        .addStatement(codeToSetColumnValuesFromResultSet)
                        .build())
                .build();

        CodeBlock.Builder codeBlockForJdbcQuery = CodeBlock.builder();
        codeBlockForJdbcQuery.add(jdbcTemplateInstanceFieldName + ".query(SQL, sqlParamMap, " + rowCallbackHandler + ");\n");

        ClassName list = ClassName.get("java.util", "ArrayList");
        ParameterizedTypeName parameterizedTypeName = TypeUtil.getParameterizedTypeName(dtoTypeName, list);

        //todo $L can be taken from parameter list, but current code works for a poc
        CodeBlock.Builder codeBlockHavingPredicatesMapBuilder = CodeBlock.builder();
        predicateHavingLiterals.forEach(literal -> codeBlockHavingPredicatesMapBuilder.addStatement("sqlParamMap.put($S, $L)",
                literal.tableName() + CaseUtils.toCamelCase(literal.columnName(), true),
                literal.tableName() + CaseUtils.toCamelCase(literal.columnName(), true)));
        CodeBlock codeBlockHavingPredicatesMap = codeBlockHavingPredicatesMapBuilder.build();

        MethodSpec methodSpec = MethodSpec.methodBuilder("getData")
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
