package com.jfeatures.msg.controller;

import static com.jfeatures.msg.codegen.MicroServiceGenerator.getSql;

import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.SqlMetadata;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/codegen")
public class CodeGenController {


    private SqlMetadata sqlMetadata;

    public CodeGenController(SqlMetadata sqlMetadata) {
        this.sqlMetadata = sqlMetadata;
    }

    @GetMapping(
            value = "/hello",
            produces = ProjectConstants.APPLICATION_JSON
    )
    public List<ColumnMetadata> selectColumnMetadata() throws URISyntaxException, SQLException {
        String sql = getSql("sample_plain_sql_without_parameters.sql");

        List<ColumnMetadata> sqlColumnTypes = sqlMetadata.getColumnMetadata(sql);
        sqlColumnTypes.forEach(type -> log.info("{}", type));
        return sqlColumnTypes;
    }

}
