package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.codegen.util.TypeUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.text.CaseUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.io.IOException;

public class GenerateController {
    public static JavaFile createController(String businessPurposeOfSQL) throws IOException {

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


        ClassName list = ClassName.get("java.util", "List");

        ParameterizedTypeName parameterizedTypeName = TypeUtil.getParameterizedTypeName(dtoTypeName, list);

        CodeBlock serviceCodeBlock = CodeBlock.builder()
                .addStatement("return $N." + "getData()", daoInstanceFieldName)
                .build();

        MethodSpec methodSpec = MethodSpec.methodBuilder("getData")
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
                .build();

        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "controller"), controller)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

}
