package com.jfeatures.msg.sql;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DBVendorInfoTest {

    @Test
    @Disabled("Need to use tescontainers for Database tests")
    void getDatabaseProductName() throws Exception {
        String databaseProductName = DBVendorInfo.getDatabaseProductName("application_properties_file_for_tests.txt");
        Assertions.assertThat(databaseProductName).isEqualTo("Microsoft SQL Server");
    }
}
