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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.io.IOException;

import static com.jfeatures.msg.codegen.util.NameUtil.getJavaClassTypeName;

public class GenerateDao {
    public static JavaFile createDao(String businessPurposeOfSQL) throws IOException {

        String jdbcTemplateInstanceFieldName = "namedParameterJdbcTemplate";


        CodeBlock codeblock = CodeBlock.builder()
                .addStatement("this.$N = $N", jdbcTemplateInstanceFieldName, jdbcTemplateInstanceFieldName) //not working correctly
                .build();
        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                .addParameter(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName)
                .addCode(codeblock)
                .build();


        FieldSpec fieldSpec = FieldSpec.builder(NamedParameterJdbcTemplate.class, jdbcTemplateInstanceFieldName, Modifier.PRIVATE, Modifier.FINAL).build();


        ClassName list = ClassName.get("java.util", "List");

        TypeName dtoTypeName = TypeUtil.getJavaClassTypeName(businessPurposeOfSQL, "dto", "DTO");
        ParameterizedTypeName parameterizedTypeName = TypeUtil.getParameterizedTypeName(dtoTypeName, list);

        //todo add method to generate getData method
        MethodSpec methodSpec = MethodSpec.methodBuilder("getData")
                .addModifiers(Modifier.PUBLIC)
                .returns(parameterizedTypeName)
                .addStatement("return null")
                .build();


        TypeSpec dao = TypeSpec.classBuilder(businessPurposeOfSQL + "DAO")
                .addModifiers(Modifier.PUBLIC)
                .addField(fieldSpec)
                .addAnnotation(Component.class)
                .addMethod(methodSpec)
                .addMethod(constructorSpec)
                .build();

        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "dao"), dao)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }
}
