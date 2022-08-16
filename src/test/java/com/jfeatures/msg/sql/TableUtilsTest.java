package com.jfeatures.msg.sql;

import com.jfeatures.msg.codegen.domain.DBColumn;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

class TableUtilsTest {

    @Test
    void testGetColumnsPerTableNameShouldReturnCorrectResult() throws SQLException, IOException, ClassNotFoundException {
        Map<String, Map<String, DBColumn>> columnsPerTableName = TableUtils.getColumnsPerTableName("application_properties_file.txt");
        Assertions.assertThat(columnsPerTableName.size()).isEqualTo(21);
        Assertions.assertThat(columnsPerTableName.get("country").get("country_id").jdbcType()).isEqualTo("Int");
    }

}
