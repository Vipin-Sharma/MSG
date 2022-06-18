package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.TableColumn;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
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
     *                             todo check support needs to be provided for same columnName column or not
     */
    public static JavaFile getDTOForMultiTableSQL(String sql, Map<String, String> ddlPerTableName, String businessPurposeOfSQL) throws JSQLParserException, IOException {
        Map<TableColumn, ColumnDefinition> columnNameToTypeMapping = MsgSqlParser.dataTypePerColumnWithTableInfo(sql, ddlPerTableName);
        List<String> selectColumns = MsgSqlParser.getSelectColumns(sql);

        List<FieldSpec> fieldSpecList = generateFieldSpecsForColumnDefinition(columnNameToTypeMapping);
        //List<MethodSpec> methodSpecList = generateMethodSpecsForColumnDefinition(columnNameToTypeMapping);

        //todo we may not need constructor for this class as we use lombok builder
        MethodSpec constructorSpec = generateConstructorSpec(columnNameToTypeMapping);

        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL + "DTO").
                addModifiers(Modifier.PUBLIC).
                addFields(fieldSpecList).
                //addMethods(methodSpecList).
                addMethod(constructorSpec).
                addAnnotation(AnnotationSpec.builder(Builder.class).addMember("builderClassName", "$S", "Builder").build()).
                addAnnotation(AnnotationSpec.builder(Getter.class).build()).
                addAnnotation(AnnotationSpec.builder(Setter.class).build()).
                build();

        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "dto"), dao)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

    private static MethodSpec generateConstructorSpec(Map<TableColumn, ColumnDefinition> columnNameToTypeMapping) {
        CodeBlock codeblock = CodeBlock.builder().build();
        List<ParameterSpec> parameterSpecList = new ArrayList<>();

        for (TableColumn tableColumn : columnNameToTypeMapping.keySet()) {
            codeblock = codeblock.toBuilder()
                    .addStatement("this.$L = $L", tableColumn.tableName() + CaseUtils.toCamelCase(tableColumn.columnName(), true),
                            tableColumn.tableName() + CaseUtils.toCamelCase(tableColumn.columnName(), true))
                    .build();
            parameterSpecList.add(ParameterSpec.builder(getClassForType(StringUtils.substringBefore(columnNameToTypeMapping.get(tableColumn).getColDataType().toString(), "(").trim()),
                    tableColumn.tableName() + CaseUtils.toCamelCase(tableColumn.columnName(), true)).build());
        }

        return MethodSpec.constructorBuilder()
                .addParameters(parameterSpecList)
                .addCode(codeblock)
                .build();
    }

    private static List<MethodSpec> generateMethodSpecsForColumnDefinition(Map<TableColumn, ColumnDefinition> columnNameToTypeMapping) {
        ArrayList<MethodSpec> methodSpecList = new ArrayList<>();

        for (TableColumn tableColumn : columnNameToTypeMapping.keySet()) {
            MethodSpec methodSpec = MethodSpec.methodBuilder("get" + StringUtils.capitalize(tableColumn.tableName()) + CaseUtils.toCamelCase(tableColumn.columnName(),true))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(getClassForType(columnNameToTypeMapping.get(tableColumn).getColDataType().getDataType()))
                    .addStatement("return $L", tableColumn.tableName() + CaseUtils.toCamelCase(tableColumn.columnName(), true))
                    .build();
            methodSpecList.add(methodSpec);
        }

        return methodSpecList;
    }

    private static List<FieldSpec> generateFieldSpecsForColumnDefinition(Map<TableColumn, ColumnDefinition> columnNameToTypeMapping) {
        ArrayList<FieldSpec> fieldSpecList = new ArrayList<>();
        for (TableColumn tableColumn : columnNameToTypeMapping.keySet()) {
            FieldSpec fieldSpec = FieldSpec.builder(getClassForType(columnNameToTypeMapping.get(tableColumn).getColDataType().getDataType()),
                            tableColumn.tableName() + CaseUtils.toCamelCase(tableColumn.columnName(), true))
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build();
            fieldSpecList.add(fieldSpec);
        }
        return fieldSpecList;
    }

}
