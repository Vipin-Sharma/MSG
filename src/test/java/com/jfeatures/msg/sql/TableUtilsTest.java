package com.jfeatures.msg.sql;

import com.jfeatures.msg.codegen.domain.DBColumn;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TableUtilsTest {

    @Test
    @Disabled("Need to use tescontainers for Database tests")
    void testGetColumnsPerTableNameShouldReturnCorrectResult() throws Exception {
        Map<String, Map<String, DBColumn>> columnsPerTableName = TableUtils.getColumnsPerTableName("application_properties_file.txt");
        assertThat(columnsPerTableName).hasSize(21);
        assertThat(columnsPerTableName.get("country").get("country_id").jdbcType()).isEqualTo("Int");
    }

}
