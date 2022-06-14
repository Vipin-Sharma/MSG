package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.domain.TableColumn;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.codegen.util.TypeUtil;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.squareup.javapoet.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
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


        FieldSpec fieldSpec = FieldSpec.builder(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName, Modifier.PRIVATE, Modifier.FINAL).build();

        TypeName dtoTypeName = TypeUtil.getJavaClassTypeName(businessPurposeOfSQL, "dto", "DTO");

        //todo add method to generate getData method
        ArrayList<ParameterSpec> parameters = new ArrayList<>();

        predicateHavingLiterals.forEach(literal ->
                parameters.add(ParameterSpec.builder(ClassName.bestGuess(literal.type()).box(), literal.name()).build()));

        CodeBlock.Builder codeBlockHavingPredicatesMapBuilder = CodeBlock.builder();
        predicateHavingLiterals.forEach(literal -> codeBlockHavingPredicatesMapBuilder.addStatement("sqlParamMap.put($S, $L)", literal.name(), literal.name()));
        CodeBlock codeBlockHavingPredicatesMap = codeBlockHavingPredicatesMapBuilder.build();

        CodeBlock codeBlock = CodeBlock.builder()
                .addStatement("$T<String, Object> sqlParamMap = new $T()", Map.class, HashMap.class)
                .build();

        /*ddlPerTableName.keySet().forEach(tableName -> {
            String ddl = ddlPerTableName.get(tableName);

                }*/

        TypeName builderTypeForDto = getBuilderType(dtoTypeName);
        TypeSpec rowCallbackHandler = TypeSpec
                .anonymousClassBuilder("")
                .addSuperinterface(RowCallbackHandler.class)
                .addMethod(MethodSpec.methodBuilder("processRow")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ResultSet.class, "rs")
                        .addException(SQLException.class)
                        .addStatement("$T " + ((ClassName) builderTypeForDto).simpleName() + " = $T.builder()", builderTypeForDto, dtoTypeName)
                        /*.addCode()*/
                        .build())
                .build();

        CodeBlock.Builder codeBlockForJdbcQuery = CodeBlock.builder();
        codeBlockForJdbcQuery.add(jdbcTemplateInstanceFieldName + ".query(\"select * from " + businessPurposeOfSQL + "\", sqlParamMap, " + rowCallbackHandler);

        ClassName list = ClassName.get("java.util", "ArrayList");
        ParameterizedTypeName parameterizedTypeName = TypeUtil.getParameterizedTypeName(dtoTypeName, list);

        Map<TableColumn, ColumnDefinition> columnNameToTypeMapping = MsgSqlParser.dataTypePerColumnWithTableInfo(sql, ddlPerTableName);

        MethodSpec methodSpec = MethodSpec.methodBuilder("getData")
                .addStatement("$T result = new $T()", parameterizedTypeName, ArrayList.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameters)
                .returns(parameterizedTypeName)
                .addCode(codeBlock)
                .addCode(codeBlockHavingPredicatesMap)
                .addCode(codeBlockForJdbcQuery.build())
                .addStatement("return result")
                .build();


        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL + "DAO")
                .addModifiers(Modifier.PUBLIC)
                .addField(fieldSpec)
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