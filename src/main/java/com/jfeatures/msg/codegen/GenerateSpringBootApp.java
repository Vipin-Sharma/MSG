package com.jfeatures.msg.codegen;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import org.apache.commons.text.CaseUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenerateSpringBootApp {
    public static JavaFile createSpringBootApp(String sql, Map<String, String> ddlPerTableName, String businessPurposeOfSQL) throws IOException {

        MethodSpec methodSpec = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addStatement("SpringApplication.run(" + businessPurposeOfSQL + "SpringBootApplication.class, args)")
                .build();

        TypeSpec mainClass = TypeSpec.classBuilder(businessPurposeOfSQL + "SpringBootApplication").
                addModifiers(Modifier.PUBLIC).
                addMethod(methodSpec).
                build();

        JavaFile javaFile = JavaFile.builder("com.jfeatures.msg", mainClass)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

    private static List<MethodSpec> generateMethodSpecsForColumnDefinition(Map<String, ColumnDefinition> columnNameToTypeMapping, List<String> selectColumns) {
        ArrayList<MethodSpec> methodSpecList = new ArrayList<>();

        for (String selectColumn : selectColumns) {
            MethodSpec methodSpec = MethodSpec.methodBuilder("get" + CaseUtils.toCamelCase(selectColumn, true))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(SQLServerDataTypeEnum.getClassForType(columnNameToTypeMapping.get(selectColumn).getColDataType().getDataType()))
                    .addStatement("return $L", CaseUtils.toCamelCase(selectColumn, false))
                    .build();
            methodSpecList.add(methodSpec);
        }

        return methodSpecList;
    }
}
