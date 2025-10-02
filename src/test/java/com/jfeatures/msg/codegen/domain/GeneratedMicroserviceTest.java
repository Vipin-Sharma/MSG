package com.jfeatures.msg.codegen.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeneratedMicroserviceTest {

    private JavaFile sampleFile;

    @BeforeEach
    void setUp() {
        TypeSpec type = TypeSpec.classBuilder("Sample").addModifiers(Modifier.PUBLIC).build();
        sampleFile = JavaFile.builder("com.example", type).build();
    }

    @Test
    void constructorValidatesArguments() {
        assertThrows(IllegalArgumentException.class, () ->
            new GeneratedMicroservice(null, sampleFile, sampleFile, sampleFile, sampleFile, "config", SqlStatementType.SELECT));

        assertThrows(IllegalArgumentException.class, () ->
            new GeneratedMicroservice(" ", sampleFile, sampleFile, sampleFile, sampleFile, "config", SqlStatementType.SELECT));

        assertThrows(IllegalArgumentException.class, () ->
            new GeneratedMicroservice("Customer", null, sampleFile, sampleFile, sampleFile, "config", SqlStatementType.SELECT));

        assertThrows(IllegalArgumentException.class, () ->
            new GeneratedMicroservice("Customer", sampleFile, null, sampleFile, sampleFile, "config", SqlStatementType.SELECT));

        assertThrows(IllegalArgumentException.class, () ->
            new GeneratedMicroservice("Customer", sampleFile, sampleFile, null, sampleFile, "config", SqlStatementType.SELECT));

        assertThrows(IllegalArgumentException.class, () ->
            new GeneratedMicroservice("Customer", sampleFile, sampleFile, sampleFile, null, "config", SqlStatementType.SELECT));

        assertThrows(IllegalArgumentException.class, () ->
            new GeneratedMicroservice("Customer", sampleFile, sampleFile, sampleFile, sampleFile, null, SqlStatementType.SELECT));

        assertThrows(IllegalArgumentException.class, () ->
            new GeneratedMicroservice("Customer", sampleFile, sampleFile, sampleFile, sampleFile, " ", SqlStatementType.SELECT));

        assertThrows(IllegalArgumentException.class, () ->
            new GeneratedMicroservice("Customer", sampleFile, sampleFile, sampleFile, sampleFile, "config", null));
    }

    @Test
    void constructorAcceptsValidValues() {
        assertDoesNotThrow(() ->
            new GeneratedMicroservice("Customer", sampleFile, sampleFile, sampleFile, sampleFile, "config", SqlStatementType.SELECT));
    }
}
