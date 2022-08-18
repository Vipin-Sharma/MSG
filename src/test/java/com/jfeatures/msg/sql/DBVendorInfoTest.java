package com.jfeatures.msg.sql;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

class DBVendorInfoTest {

    @Test
    void getDatabaseProductName() throws SQLException, IOException {
        String databaseProductName = DBVendorInfo.getDatabaseProductName("application_properties_file_for_tests.txt");
        Assertions.assertThat(databaseProductName).isEqualTo("Microsoft SQL Server");
    }
}
