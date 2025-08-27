package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.codegen.util.TypeUtil;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.text.CaseUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenerateController {
    public static JavaFile createController(String businessPurposeOfSQL, List<DBColumn> predicateHavingLiterals) throws IOException
    {
        TypeName daoTypeName = TypeUtil.getJavaClassTypeName(businessPurposeOfSQL, "dao", "DAO");
        TypeName dtoTypeName = TypeUtil.getJavaClassTypeName(businessPurposeOfSQL, "dto", "DTO");

        String daoInstanceFieldName = CaseUtils.toCamelCase(businessPurposeOfSQL, false) + "DAO";
        FieldSpec fieldSpec = FieldSpec.builder(daoTypeName, daoInstanceFieldName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();

        CodeBlock codeblock = CodeBlock.builder()
                .addStatement("this.$N = $N", daoInstanceFieldName, daoInstanceFieldName) //not working correctly
                .build();
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(daoTypeName, daoInstanceFieldName)
                .addCode(codeblock)
                .build();


        ClassName listClass = ClassName.get("java.util", "List");

        ParameterizedTypeName parameterizedTypeName = TypeUtil.getParameterizedTypeName(dtoTypeName, listClass);
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        ArrayList<String> getDataParameters       = predicateHavingLiterals.stream().collect(ArrayList::new,
                (list, dbColumn) -> list.add(CaseUtils.toCamelCase(dbColumn.columnName(), false)),
                ArrayList::addAll);
        String getDataMethodParametersString = getDataParameters.stream().reduce((a, b) -> a + ", " + b).orElse("");

        predicateHavingLiterals.forEach(literal ->
                parameterSpecs.add(ParameterSpec.builder(ClassName.bestGuess(literal.javaType()).box(),
                                CaseUtils.toCamelCase(literal.columnName(), false))
                                .addAnnotation(AnnotationSpec.builder(RequestParam.class).addMember("value", "$S",
                                        CaseUtils.toCamelCase(literal.columnName(), false)).build())
                        .build()));

        CodeBlock serviceCodeBlock = CodeBlock.builder()
                .addStatement("return $N." + "get"+ businessPurposeOfSQL + "(" + getDataMethodParametersString + ")", daoInstanceFieldName)
                .build();

        MethodSpec methodSpec = MethodSpec.methodBuilder("getDataFor" + businessPurposeOfSQL)
                .addAnnotation(AnnotationSpec.builder(GetMapping.class)
                        .addMember("value", "$S", "/"+ businessPurposeOfSQL)
                        .addMember("produces", "$S", "application/json")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Operation.class)
                        .addMember("summary", "$S", "Get API to fetch data for " + businessPurposeOfSQL)
                        .build())
                .addParameters(parameterSpecs)
                .addModifiers(Modifier.PUBLIC)
                .addCode(serviceCodeBlock)
                .returns(parameterizedTypeName)
                .build();


        TypeSpec controller = TypeSpec.classBuilder(businessPurposeOfSQL + "Controller")
                .addModifiers(Modifier.PUBLIC)
                .addField(fieldSpec)
                .addMethod(methodSpec)
                .addMethod(constructorSpec)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("path", "$S", "/api")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Tag.class)
                        .addMember("name", "$S", businessPurposeOfSQL)
                        .addMember("description", "$S", businessPurposeOfSQL)
                        .build())
                .build();

        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "controller"), controller)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

}
