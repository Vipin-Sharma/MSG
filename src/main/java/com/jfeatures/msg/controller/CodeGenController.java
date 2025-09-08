package com.jfeatures.msg.controller;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.SqlMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import static com.jfeatures.msg.codegen.MicroServiceGenerator.getSql;

@Slf4j
@RestController
@RequestMapping(path = "/codegen")
//@AutoConfiguration
public class CodeGenController {


    private SqlMetadata sqlMetadata;

    public CodeGenController(SqlMetadata sqlMetadata) {
        this.sqlMetadata = sqlMetadata;
    }

    @GetMapping(
            value = "/hello",
            produces = "application/json"
    )
    public List<ColumnMetadata> selectColumnMetadata() throws URISyntaxException, SQLException {
        //String sql = getSql("sample_parameterized_sql.sql");
        String sql = getSql("sample_plain_sql_without_parameters.sql");

        List<ColumnMetadata> sqlColumnTypes = sqlMetadata.getColumnMetadata(sql);
        sqlColumnTypes.forEach(type -> log.info("{}", type));
        return sqlColumnTypes;
    }

}
