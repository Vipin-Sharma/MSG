package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;

@Slf4j
public class GenerateDTO {


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

        JavaFile javaFile = JavaFile.builder(JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "dto"), dto)
                .build();

        log.info(javaFile.toString());

        return javaFile;
    }

    private static ArrayList<FieldSpec> generateFieldSpecsFromColumnMetadata(List<ColumnMetadata> selectColumnMetadata) throws ClassNotFoundException {
        ArrayList<FieldSpec> fieldSpecList = new ArrayList<>();
        for (ColumnMetadata columnMetadata : selectColumnMetadata) {
            Class<?> type = getClassFromStringType(columnMetadata.getColumnClassName());
            String rawFieldName = columnMetadata.getColumnAlias() != null ? columnMetadata.getColumnAlias() : columnMetadata.getColumnName();
            // Convert snake_case to camelCase for proper Java field naming, but preserve existing camelCase
            String fieldName = rawFieldName.contains("_") ? CaseUtils.toCamelCase(rawFieldName, false, '_') : rawFieldName;
            FieldSpec fieldSpec = FieldSpec.builder(type, fieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .build();
            fieldSpecList.add(fieldSpec);
        }
        return fieldSpecList;
    }

    private static Class<?> getClassFromStringType(String columnClassName) throws ClassNotFoundException {
        return Class.forName(columnClassName);
    }
}
