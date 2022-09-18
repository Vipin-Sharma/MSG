package com.jfeatures.msg.codegen;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.lang.model.element.Modifier;
import java.io.IOException;

public class GenerateSpringBootApp {
    public static JavaFile createSpringBootApp(String businessPurposeOfSQL) throws IOException {

        MethodSpec methodSpec = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String[].class, "args")
                .returns(TypeName.VOID)
                .addStatement("$T.run(" + businessPurposeOfSQL + "SpringBootApplication.class, args)", SpringApplication.class)
                .build();

        TypeSpec mainClass = TypeSpec.classBuilder(businessPurposeOfSQL + "SpringBootApplication").
                addModifiers(Modifier.PUBLIC).
                addMethod(methodSpec).
                addAnnotation(SpringBootApplication.class).
                build();

        JavaFile javaFile = JavaFile.builder("com.jfeatures.msg", mainClass)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

}
