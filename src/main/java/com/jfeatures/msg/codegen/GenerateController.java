package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.util.NameUtil;
import com.jfeatures.msg.codegen.util.TypeUtil;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.squareup.javapoet.*;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.text.CaseUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenerateController {
    public static JavaFile createController(String sql, String businessPurposeOfSQL, Map<String, String> ddlPerTableName) throws IOException, JSQLParserException {

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

        List<DBColumn> predicateHavingLiterals = MsgSqlParser.extractPredicateHavingLiterals(sql, ddlPerTableName);
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        predicateHavingLiterals.forEach(literal ->
                parameterSpecs.add(ParameterSpec.builder(ClassName.bestGuess(literal.type()).box(), literal.name())
                                .addAnnotation(AnnotationSpec.builder(RequestParam.class).addMember("value", "$S", literal.name()).build())
                        .build()));

        MethodSpec methodSpec = MethodSpec.methodBuilder("getData")
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("value", "$S", "/api")
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
                .build();

        JavaFile javaFile = JavaFile.builder(NameUtil.getPackageName(businessPurposeOfSQL, "controller"), controller)
                .build();

        javaFile.writeTo(System.out);

        return javaFile;
    }

}
