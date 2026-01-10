package com.jfeatures.msg.customer.dao;

import com.jfeatures.msg.customer.dto.CustomerUpdateDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.lang.Integer;
import java.lang.String;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerUpdateDAO {
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

  /**
   * Constructor with dependency injection for JDBC template.\n@param namedParameterJdbcTemplate the named parameter JDBC template
   */
  public CustomerUpdateDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  /**
   * Updates customer record(s) in the database.
   * @param updateDto The data to update
   * @param request HTTP request for user authentication
   * @return Number of rows updated
   */
  public int updateCustomer(@Valid CustomerUpdateDTO updateDto, Integer id, String status,
      HttpServletRequest request) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("firstName", updateDto.getFirstName());
    paramMap.put("lastName", updateDto.getLastName());
    paramMap.put("email", updateDto.getEmail());

    // Auto-populate audit fields
    paramMap.put("lastUpdate", LocalDateTime.now());
    paramMap.put("id", id);
    paramMap.put("status", status);

    log.info("Executing UPDATE: {}", SQL);
    log.debug("Parameters: {}", paramMap);

    int rowsUpdated = namedParameterJdbcTemplate.update(SQL, paramMap);
    log.info("Updated {} rows for {}", rowsUpdated, "Customer");
    return rowsUpdated;
  }
}
