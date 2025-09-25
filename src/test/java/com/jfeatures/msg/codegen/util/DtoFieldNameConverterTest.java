package com.jfeatures.msg.codegen.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jfeatures.msg.codegen.domain.TableColumn;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DtoFieldNameConverterTest {

    @ParameterizedTest(name = "Table column {0} converts to {1}")
    @MethodSource("tableColumnConversions")
    void testConvertToJavaCamelCase_TableColumn(TableColumn column, String expectedValue) {
        String result = DtoFieldNameConverter.convertToJavaCamelCase(column);

        assertEquals(expectedValue, result);
    }

    private static Stream<Arguments> tableColumnConversions() {
        return Stream.of(
            Arguments.of(new TableColumn("customer_id", "customerId", "table"), "customerId"),
            Arguments.of(new TableColumn("customer_name", null, "table"), "customerName"),
            Arguments.of(new TableColumn("first_name_last_name", null, "table"), "firstNameLastName"),
            Arguments.of(new TableColumn("email", null, "table"), "email"),
            Arguments.of(new TableColumn("customerName", null, "table"), "customername"),
            Arguments.of(new TableColumn("customer_id", "", "table"), "")
        );
    }

    @ParameterizedTest(name = "Database column {0} converts to {1}")
    @MethodSource("databaseColumnConversions")
    void testConvertToJavaCamelCase_String(String databaseColumnName, String expectedValue) {
        String result = DtoFieldNameConverter.convertToJavaCamelCase(databaseColumnName);

        assertEquals(expectedValue, result);
    }

    private static Stream<Arguments> databaseColumnConversions() {
        return Stream.of(
            Arguments.of("customer_id", "customerId"),
            Arguments.of("date_of_birth_timestamp", "dateOfBirthTimestamp"),
            Arguments.of("email", "email"),
            Arguments.of("customerName", "customername"),
            Arguments.of("customer_id_123", "customerId123"),
            Arguments.of("_customer_id", "customerId"),
            Arguments.of("customer_id_", "customerId"),
            Arguments.of("customer__id___name", "customerIdName"),
            Arguments.of("CUSTOMER_ID", "customerId"),
            Arguments.of("Customer_ID_Name", "customerIdName"),
            Arguments.of("___", ""),
            Arguments.of("order_by_date", "orderByDate"),
            Arguments.of("very_long_database_column_name_with_many_words", "veryLongDatabaseColumnNameWithManyWords")
        );
    }

    @ParameterizedTest(name = "Invalid database column {0} throws exception")
    @MethodSource("invalidDatabaseColumns")
    void testConvertToJavaCamelCase_InvalidInputs(String invalidInput) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DtoFieldNameConverter.convertToJavaCamelCase(invalidInput)
        );

        assertEquals("Database column name cannot be null or empty", exception.getMessage());
    }

    private static Stream<String> invalidDatabaseColumns() {
        return Stream.of(null, "", "   ");
    }

    @ParameterizedTest(name = "Null table column throws exception")
    @MethodSource("nullTableColumns")
    void testConvertToJavaCamelCase_NullTableColumn(TableColumn column) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DtoFieldNameConverter.convertToJavaCamelCase(column)
        );

        assertEquals("Database table column cannot be null", exception.getMessage());
    }

    private static Stream<TableColumn> nullTableColumns() {
        return Stream.of((TableColumn) null);
    }
}