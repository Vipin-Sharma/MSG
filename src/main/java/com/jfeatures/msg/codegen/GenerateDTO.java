package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.domain.TableColumn;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jfeatures.msg.codegen.SQLServerDataTypeEnum.getClassForType;

public class GenerateDTO {
    static final int MAX_NUMBER_PARAMETERS_METHOD = 255;
    /**
     * @param sql                  SQL to generate DTO
     * @param ddlPerTableName      DDL per Table.
     * @param businessPurposeOfSQL It can be a string like productCatalog
     * @throws JSQLParserException This is standard RuntimeException from SQL parser
     */
    public static JavaFile dtoFromSqlAndDdl(String sql, Map<String, String> ddlPerTableName, String businessPurposeOfSQL) throws JSQLParserException, IOException {
        Map<TableColumn, ColumnDefinition> columnNameToTypeMapping = MsgSqlParser.dataTypePerColumnWithTableInfo(sql, ddlPerTableName);

        List<FieldSpec> fieldSpecList = generateFieldSpecsFromColumnMetadata(columnNameToTypeMapping);

        TypeSpec dto = getDto(businessPurposeOfSQL, columnNameToTypeMapping, fieldSpecList);

        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "dto"), dto)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }


    /**
     *
     * @param businessPurposeOfSQL It can be a string like productCatalog
     * @param columnNameToTypeMapping map with columns details ( columnName, Alias, tableName) and definition ( NVARCHAR,
     *    * INT)
     * @param fieldSpecList List of fields to be created. It will use Lombok if size <= 255 and POJO if not.
     * @return A generated DTO represent by TypeSpec
     */
    private static TypeSpec getDto(String businessPurposeOfSQL, Map<TableColumn, ColumnDefinition> columnNameToTypeMapping, List<FieldSpec> fieldSpecList) {

        TypeSpec dto;
        if(fieldSpecList.size() <= MAX_NUMBER_PARAMETERS_METHOD)
        {
            dto = TypeSpec.classBuilder(businessPurposeOfSQL + "DTO").
                    addModifiers(Modifier.PUBLIC).
                    addFields(fieldSpecList).
                    addAnnotation(AnnotationSpec.builder(Builder.class).addMember("builderClassName", "$S", "Builder").build()).
                    addAnnotation(AnnotationSpec.builder(Value.class).build()).
                    addAnnotation(AnnotationSpec.builder(Jacksonized.class).build()).
                    build();
        }
        else
        {
            List<MethodSpec> methodSpecList = generateMethodSpecsForColumnDefinition(columnNameToTypeMapping);
            MethodSpec constructorSpec = generateConstructorSpec();

            dto = TypeSpec.classBuilder(businessPurposeOfSQL + "DTO").
                    addModifiers(Modifier.PUBLIC, Modifier.FINAL).
                    addFields(fieldSpecList).
                    addMethods(methodSpecList).
                    addMethod(constructorSpec).
                    build();
        }

        return dto;
    }

    private static List<FieldSpec> generateFieldSpecsFromColumnMetadata(Map<TableColumn, ColumnDefinition> columnNameToTypeMapping) {
        ArrayList<FieldSpec> fieldSpecList = new ArrayList<>();
        for (Map.Entry<TableColumn, ColumnDefinition> entry : columnNameToTypeMapping.entrySet()) {
            Class<?> type = getClassForType(entry.getValue().getColDataType().getDataType());
            String fieldName = NameUtil.getFieldNameForDTO(entry.getKey());
            FieldSpec fieldSpec = FieldSpec.builder(type, fieldName)
                    .build();
            fieldSpecList.add(fieldSpec);
        }
        return fieldSpecList;
    }

    private static MethodSpec generateConstructorSpec() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    private static List<MethodSpec> generateMethodSpecsForColumnDefinition(Map<TableColumn, ColumnDefinition> columnNameToTypeMapping) {
        ArrayList<MethodSpec> methodSpecList = new ArrayList<>();

        for (Map.Entry<TableColumn, ColumnDefinition> entry : columnNameToTypeMapping.entrySet()) {
            TableColumn tableColumn = entry.getKey();
            MethodSpec methodSpec = MethodSpec.methodBuilder("get" + NameUtil.getFieldNameForDTO(tableColumn))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(getClassForType(entry.getValue().getColDataType().getDataType()))
                    .addStatement("return $L", NameUtil.getFieldNameForDTO(tableColumn))
                    .build();
            methodSpecList.add(methodSpec);
        }

        for (Map.Entry<TableColumn, ColumnDefinition> entry : columnNameToTypeMapping.entrySet()) {
            TableColumn tableColumn = entry.getKey();
            MethodSpec methodSpec = MethodSpec.methodBuilder("set" + NameUtil.getFieldNameForDTO(tableColumn))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(getClassForType(entry.getValue().getColDataType().getDataType()), NameUtil.getFieldNameForDTO(tableColumn))
                    .addStatement("this." + NameUtil.getFieldNameForDTO(tableColumn) + " = " + NameUtil.getFieldNameForDTO(tableColumn))
                    .build();
            methodSpecList.add(methodSpec);
        }

        return methodSpecList;
    }


    /**
     * This method has limitation of number of fields lesser than 255, due to Lombok limitation. After that, we will need to generate POJO.
     */
    public static JavaFile dtoFromColumnMetadata(List<ColumnMetadata> selectColumnMetadata, String businessPurposeOfSQL) throws ClassNotFoundException, IOException {
        ArrayList<FieldSpec> fieldSpecs = generateFieldSpecsFromColumnMetadata(selectColumnMetadata);

        TypeSpec dto = TypeSpec.classBuilder(businessPurposeOfSQL + "DTO").
                addModifiers(Modifier.PUBLIC).
                addFields(fieldSpecs).
                addAnnotation(AnnotationSpec.builder(Builder.class).addMember("builderClassName", "$S", "Builder").build()).
                addAnnotation(AnnotationSpec.builder(Value.class).build()).
                addAnnotation(AnnotationSpec.builder(Jacksonized.class).build()).
                build();

        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "dto"), dto)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

    private static ArrayList<FieldSpec> generateFieldSpecsFromColumnMetadata(List<ColumnMetadata> selectColumnMetadata) throws ClassNotFoundException {
        ArrayList<FieldSpec> fieldSpecList = new ArrayList<>();
        for (ColumnMetadata columnMetadata : selectColumnMetadata) {
            Class<?> type = getClassFromStringType(columnMetadata.getColumnClassName());
            String fieldName = columnMetadata.getColumnAlias() != null ? columnMetadata.getColumnAlias() : columnMetadata.getColumnName();
            FieldSpec fieldSpec = FieldSpec.builder(type, fieldName)
                    .build();
            fieldSpecList.add(fieldSpec);
        }
        return fieldSpecList;
    }

    private static Class<?> getClassFromStringType(String columnClassName) throws ClassNotFoundException {
        return Class.forName(columnClassName);
    }
}
