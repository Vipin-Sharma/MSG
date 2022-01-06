package com.jfeatures.msg.codegen;

import com.jfeatures.msg.sql.MsgDdlParser;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
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
     * @param sql
     * @param ddl
     * @param businessPurposeOfSQL It can be a string like productCatalog
     * @throws JSQLParserException
     */
    public static JavaFile getDTO(String sql, String ddl, String businessPurposeOfSQL) throws JSQLParserException, IOException {
        Map<String, ColDataType> columnNameTypeMap = MsgDdlParser.parseDdl(ddl);
        List<String> tables = MsgSqlParser.getTablesFromSQL(sql);
        List<String> selectColumns = MsgSqlParser.getSelectColumns(sql);

        ArrayList<FieldSpec> fieldSpecList = selectColumns
                .stream()
                .collect(ArrayList::new,
                        (fieldSpecs, columnName) -> {
                            FieldSpec fieldSpec = FieldSpec.builder(TypeEnum.getClassForType(columnNameTypeMap.get(columnName).getDataType()), CaseUtils.toCamelCase(columnName, false))
                                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                                    .build();
                            fieldSpecs.add(fieldSpec);
                        },
                        ArrayList::addAll);

        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL+ "DTO").
                addModifiers(Modifier.PUBLIC, Modifier.FINAL).
                addFields(fieldSpecList).
                build();

        JavaFile javaFile = JavaFile.builder("com.jfeatures.msg."+ businessPurposeOfSQL, dao)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }
}
