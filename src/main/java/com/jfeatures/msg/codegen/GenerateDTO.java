package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.TableColumn;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
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
     */
    public static JavaFile dtoFromSqlAndDdl(String sql, Map<String, String> ddlPerTableName, String businessPurposeOfSQL) throws JSQLParserException, IOException {
        Map<TableColumn, ColumnDefinition> columnNameToTypeMapping = MsgSqlParser.dataTypePerColumnWithTableInfo(sql, ddlPerTableName);

        List<FieldSpec> fieldSpecList = generateFieldSpecsForColumnDefinition(columnNameToTypeMapping);

        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL + "DTO").
                addModifiers(Modifier.PUBLIC).
                addFields(fieldSpecList).
                addAnnotation(AnnotationSpec.builder(Builder.class).addMember("builderClassName", "$S", "Builder").build()).
                addAnnotation(AnnotationSpec.builder(Value.class).build()).
                addAnnotation(AnnotationSpec.builder(Jacksonized.class).build()).
                build();

        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "dto"), dao)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

    private static List<FieldSpec> generateFieldSpecsForColumnDefinition(Map<TableColumn, ColumnDefinition> columnNameToTypeMapping) {
        ArrayList<FieldSpec> fieldSpecList = new ArrayList<>();
        for (TableColumn tableColumn : columnNameToTypeMapping.keySet()) {
            Class<?> type = getClassForType(columnNameToTypeMapping.get(tableColumn).getColDataType().getDataType());
            String fieldName = tableColumn.columnAliasIfAvailable() != null ? tableColumn.columnAliasIfAvailable(): tableColumn.columnName();
            String fieldNameInCamelCase = CaseUtils.toCamelCase(fieldName, false, '_');
            FieldSpec fieldSpec = FieldSpec.builder(type, fieldNameInCamelCase)
                    .build();
            fieldSpecList.add(fieldSpec);
        }
        return fieldSpecList;
    }

}
