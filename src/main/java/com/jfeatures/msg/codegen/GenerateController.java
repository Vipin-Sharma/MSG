package com.jfeatures.msg.codegen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.text.CaseUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;

public class GenerateController {
    public static JavaFile createController(String businessPurposeOfSQL) throws IOException {

        // todo package name and Type name can go into an util class, as this will be a consistent approach in entire application.
        String daoPackage = "com.jfeatures.msg.dao";
        TypeName daoTypeName = ClassName.get(daoPackage, businessPurposeOfSQL + "DAO");
        String dtoPackage = "com.jfeatures.msg.dto";
        TypeName dtoTypeName = ClassName.get(dtoPackage, businessPurposeOfSQL + "DTO");

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


        ClassName list = ClassName.get("java.util", "List");

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(list, dtoTypeName);

        CodeBlock serviceCodeBlock = CodeBlock.builder()
                .addStatement("return $N." + "get" + businessPurposeOfSQL, daoInstanceFieldName)
                .build();

        MethodSpec methodSpec = MethodSpec.methodBuilder("get" + CaseUtils.toCamelCase(businessPurposeOfSQL, true))
                .addModifiers(Modifier.PUBLIC)
                .addCode(serviceCodeBlock)
                .returns(parameterizedTypeName)
                .build();


        TypeSpec controller = TypeSpec.classBuilder(businessPurposeOfSQL + "Controller").
                addModifiers(Modifier.PUBLIC).
                addField(fieldSpec).
                addMethod(methodSpec).
                addMethod(constructorSpec).
                build();

        JavaFile javaFile = JavaFile.builder("com.jfeatures.msg.controller", controller)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }
}
