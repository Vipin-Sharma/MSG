package com.jfeatures.msg.codegen;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SQLServerDataTypeEnumTest {

    @Test
    void shouldReturnIntegerForIntType() {
        assertThat(SQLServerDataTypeEnum.getClassForType("INT")).isEqualTo(Integer.class);
    }

    @Test
    void shouldReturnStringForCharType() {
        assertThat(SQLServerDataTypeEnum.getClassForType("CHAR")).isEqualTo(String.class);
    }

    @Test
    void shouldReturnStringForNvarcharType() {
        assertThat(SQLServerDataTypeEnum.getClassForType("NVARCHAR")).isEqualTo(String.class);
    }

    @Test
    void shouldReturnBigDecimalForDecimalType() {
        assertThat(SQLServerDataTypeEnum.getClassForType("DECIMAL")).isEqualTo(BigDecimal.class);
    }

    @Test
    void shouldReturnBooleanForBitType() {
        assertThat(SQLServerDataTypeEnum.getClassForType("BIT")).isEqualTo(Boolean.class);
    }

    @Test
    void shouldHandleCaseInsensitivity() {
        assertThat(SQLServerDataTypeEnum.getClassForType("int")).isEqualTo(Integer.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("INT")).isEqualTo(Integer.class);
        assertThat(SQLServerDataTypeEnum.getClassForType("Int")).isEqualTo(Integer.class);
    }
}