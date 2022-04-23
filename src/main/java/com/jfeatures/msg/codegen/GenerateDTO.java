package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jfeatures.msg.codegen.SQLServerDataTypeEnum.getClassForType;

public class GenerateDTO {

    /**
     * @param sql                  SQL to generate DTO
     * @param ddlPerTableName      DDL per Table.
     * @param businessPurposeOfSQL It can be a string like productCatalog
     * @throws JSQLParserException This is standard RuntimeException from SQL parser
     * todo check support needs to be provided for same name column or not
     */
    public static JavaFile getDTOForMultiTableSQL(String sql, Map<String, String> ddlPerTableName, String businessPurposeOfSQL) throws JSQLParserException, IOException {
        Map<String, ColumnDefinition> columnNameToTypeMapping = MsgSqlParser.dataTypePerColumn(sql, ddlPerTableName);
        //List<String> tables = MsgSqlParser.getTablesFromSQL(sql);
        List<String> selectColumns = MsgSqlParser.getSelectColumns(sql);

        List<FieldSpec> fieldSpecList = generateFieldSpecsForColumnDefinition(columnNameToTypeMapping, selectColumns);
        List<MethodSpec> methodSpecList = generateMethodSpecsForColumnDefinition(columnNameToTypeMapping, selectColumns);
        MethodSpec constructorSpec = generateConstructorSpec(selectColumns, columnNameToTypeMapping);

        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL + "DTO").
                addModifiers(Modifier.PUBLIC, Modifier.FINAL).
                addFields(fieldSpecList).
                addMethods(methodSpecList).
                addMethod(constructorSpec).
                build();

        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "dto"), dao)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

    private static MethodSpec generateConstructorSpec(List<String> selectColumns, Map<String, ColumnDefinition> columnNameToTypeMapping) {
        CodeBlock codeblock = CodeBlock.builder().build();
        List<ParameterSpec> parameterSpecList = new ArrayList<>();

        for (String columnName : selectColumns) {
            codeblock = codeblock.toBuilder()
                    .addStatement("this.$L = $L", CaseUtils.toCamelCase(columnName, false),
                            CaseUtils.toCamelCase(columnName, false))
                    .build();
            parameterSpecList.add(ParameterSpec.builder(getClassForType(StringUtils.substringBefore(columnNameToTypeMapping.get(columnName).getColDataType().toString(), "(").trim()),
                    CaseUtils.toCamelCase(columnName, false)).build());
        }

        return MethodSpec.constructorBuilder()
                .addParameters(parameterSpecList)
                .addCode(codeblock)
                .build();
    }

    private static List<MethodSpec> generateMethodSpecsForColumnDefinition(Map<String, ColumnDefinition> columnNameToTypeMapping, List<String> selectColumns) {
        ArrayList<MethodSpec> methodSpecList = new ArrayList<>();

        for (String selectColumn : selectColumns) {
            MethodSpec methodSpec = MethodSpec.methodBuilder("get" + CaseUtils.toCamelCase(selectColumn, true))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(getClassForType(columnNameToTypeMapping.get(selectColumn).getColDataType().getDataType()))
                    .addStatement("return $L", CaseUtils.toCamelCase(selectColumn, false))
                    .build();
            methodSpecList.add(methodSpec);
        }

        return methodSpecList;
    }

    private static List<FieldSpec> generateFieldSpecsForColumnDefinition(Map<String, ColumnDefinition> columnNameTypeMap, List<String> selectColumns) {
        ArrayList<FieldSpec> fieldSpecList = new ArrayList<>();
        for (String columnName : selectColumns) {
            FieldSpec fieldSpec = FieldSpec.builder(getClassForType(columnNameTypeMap.get(columnName).getColDataType().getDataType()), CaseUtils.toCamelCase(columnName, false))
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build();
            fieldSpecList.add(fieldSpec);
        }
        return fieldSpecList;
    }

}
