package com.jfeatures.msg.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class UtilityClassConstructorCoverageTest {

    @ParameterizedTest
    @MethodSource("utilityClasses")
    void utilityConstructorsThrowUnsupportedOperationException(Class<?> utilityClass) throws Exception {
        Constructor<?> constructor = utilityClass.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertThat(exception.getCause())
            .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Stream<Class<?>> utilityClasses() {
        return Stream.of(
            com.jfeatures.msg.codegen.GenerateController.class,
            com.jfeatures.msg.codegen.GenerateDAO.class,
            com.jfeatures.msg.codegen.GenerateDTO.class,
            com.jfeatures.msg.codegen.GenerateDatabaseConfig.class,
            com.jfeatures.msg.codegen.GenerateDeleteController.class,
            com.jfeatures.msg.codegen.GenerateDeleteDAO.class,
            com.jfeatures.msg.codegen.GenerateDeleteDTO.class,
            com.jfeatures.msg.codegen.GenerateInsertController.class,
            com.jfeatures.msg.codegen.GenerateInsertDAO.class,
            com.jfeatures.msg.codegen.GenerateInsertDTO.class,
            com.jfeatures.msg.codegen.GenerateSpringBootApp.class,
            com.jfeatures.msg.codegen.GenerateUpdateController.class,
            com.jfeatures.msg.codegen.GenerateUpdateDAO.class,
            com.jfeatures.msg.codegen.GenerateUpdateDTO.class,
            com.jfeatures.msg.codegen.sql.SqlParameterReplacer.class,
            com.jfeatures.msg.codegen.util.SqlBuilders.class,
            com.jfeatures.msg.codegen.util.DtoFieldNameConverter.class,
            com.jfeatures.msg.codegen.util.JavaPackageNameBuilder.class,
            com.jfeatures.msg.codegen.util.JavaPoetClassNameBuilder.class,
            com.jfeatures.msg.codegen.util.JavaPoetTypeNameBuilder.class,
            com.jfeatures.msg.codegen.jdbc.JdbcMethodSelector.class,
            com.jfeatures.msg.codegen.mapping.ResultSetMappingGenerator.class,
            com.jfeatures.msg.codegen.util.SqlStatementDetector.class,
            com.jfeatures.msg.sql.ReadFileFromResources.class,
            com.jfeatures.msg.codegen.util.ParameterBuilders.class,
            com.jfeatures.msg.codegen.util.FieldBuilders.class
        );
    }
}
