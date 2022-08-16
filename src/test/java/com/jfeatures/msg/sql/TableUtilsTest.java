package com.jfeatures.msg.sql;

import com.jfeatures.msg.codegen.domain.DBColumn;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

class TableUtilsTest {

    @Test
    void testGetColumnsPerTableNameShouldReturnCorrectResult() throws SQLException, IOException, ClassNotFoundException {
        Map<String, Map<String, DBColumn>> columnsPerTableName = TableUtils.getColumnsPerTableName("application_properties_file.txt");
        columnsPerTableName.forEach((key, value) -> {
            System.out.println("Table name : " + key);
            value.forEach((key1, value1) -> System.out.println(key1 + ": " + value1));
        });
    }

}
