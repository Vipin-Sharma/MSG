package com.jfeatures.msg.validname.dao;

import com.jfeatures.msg.validname.dto.ValidNameUpdateDTO;
import jakarta.validation.Valid;
import java.lang.Integer;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ValidNameUpdateDAO {
  private static final String SQL = """
  UPDATE
    customer
  SET
    first_name =: firstName,
    last_name =: lastName,
    email =: email,
    last_update =: lastUpdate
  WHERE
    id =: id
    AND param2 =: status""";

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public ValidNameUpdateDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  /**
   * Updates validname record(s) in the database.
   * @param updateDto The data to update
   * @return Number of rows updated
   */
  public int updateValidName(@Valid ValidNameUpdateDTO updateDto, Integer id, String status) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("firstName", updateDto.getFirstName());
    paramMap.put("lastName", updateDto.getLastName());
    paramMap.put("email", updateDto.getEmail());
    paramMap.put("lastUpdate", updateDto.getLastUpdate());
    paramMap.put("id", id);
    paramMap.put("status", status);

    log.info("Executing UPDATE: {}", SQL);
    log.debug("Parameters: {}", paramMap);

    int rowsUpdated = namedParameterJdbcTemplate.update(SQL, paramMap);
    log.info("Updated {} rows for {}", rowsUpdated, "ValidName");
    return rowsUpdated;
  }
}
