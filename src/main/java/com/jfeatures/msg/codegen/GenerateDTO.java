package com.jfeatures.msg.codegen;

import com.jfeatures.msg.sql.MsgDdlParser;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import org.apache.commons.text.CaseUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenerateDTO {
    /**
     * @param sql                  SQL to generate DTO
     * @param ddl                  DDL for the Table used in SQL provided. todo In case multiple table, that support should also be added
     * @param businessPurposeOfSQL It can be a string like productCatalog
     * @throws JSQLParserException This is standard RuntimeException from SQL parser
     */
    public static JavaFile getDTO(String sql, String ddl, String businessPurposeOfSQL) throws JSQLParserException, IOException {
        Map<String, ColDataType> columnNameToTypeMapping = MsgDdlParser.parseDdl(ddl);
        List<String> tables = MsgSqlParser.getTablesFromSQL(sql);
        List<String> selectColumns = MsgSqlParser.getSelectColumns(sql);

        List<FieldSpec> fieldSpecList = generateFieldSpecs(columnNameToTypeMapping, selectColumns);
        List<MethodSpec> methodSpecList = generateMethodSpecs(columnNameToTypeMapping, selectColumns);
        MethodSpec constructorSpec = generateConstructorSpec(selectColumns);

        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL + "DTO").
                addModifiers(Modifier.PUBLIC, Modifier.FINAL).
                addFields(fieldSpecList).
                addMethods(methodSpecList).
                addMethod(constructorSpec).
                build();

        JavaFile javaFile = JavaFile.builder("com.jfeatures.msg." + businessPurposeOfSQL, dao)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

    private static MethodSpec generateConstructorSpec(List<String> selectColumns) {
        CodeBlock codeblock = CodeBlock.builder().build();

        for (String columnName : selectColumns) {
            codeblock = codeblock.toBuilder()
                    .addStatement("This.$L = $L", CaseUtils.toCamelCase(columnName, false),
                            CaseUtils.toCamelCase(columnName, false))
                    .build();
        }

        return MethodSpec.constructorBuilder()
                .addCode(codeblock)
                .build();
    }

    private static List<MethodSpec> generateMethodSpecs(Map<String, ColDataType> columnNameToTypeMapping, List<String> selectColumns) {
        ArrayList<MethodSpec> methodSpecList = new ArrayList<>();

        for (String selectColumn : selectColumns) {
            MethodSpec methodSpec = MethodSpec.methodBuilder("get" + CaseUtils.toCamelCase(selectColumn, true))
                    .addModifiers(Modifier.PUBLIC)
                    //, Modifier.valueOf(TypeEnum.getClassForType(columnNameToTypeMapping.get(selectColumn).getDataType()).getName()))
                    .returns(TypeEnum.getClassForType(columnNameToTypeMapping.get(selectColumn).getDataType()))
                    .addStatement("return $L", CaseUtils.toCamelCase(selectColumn, false))
                    .build();
            methodSpecList.add(methodSpec);
        }

        return methodSpecList;
    }

    private static List<FieldSpec> generateFieldSpecs(Map<String, ColDataType> columnNameTypeMap, List<String> selectColumns) {
        ArrayList<FieldSpec> fieldSpecList = new ArrayList<>();
        for (String columnName : selectColumns) {
            FieldSpec fieldSpec = FieldSpec.builder(TypeEnum.getClassForType(columnNameTypeMap.get(columnName).getDataType()), CaseUtils.toCamelCase(columnName, false))
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build();
            fieldSpecList.add(fieldSpec);
        }
        return fieldSpecList;
    }
}
