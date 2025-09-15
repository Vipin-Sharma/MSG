package com.jfeatures.msg.validname.dto;

import jakarta.validation.constraints.NotNull;
import java.lang.String;
import java.sql.Timestamp;
import lombok.Data;

/**
 * DTO for updating validname entity.
 * Contains fields that can be updated via PUT API.
 */
@Data
public class ValidNameUpdateDTO {
  /**
   * The first name to update
   */
  @NotNull(
      message = "firstName cannot be null"
  )
  private String firstName;

  /**
   * The last name to update
   */
  @NotNull(
      message = "lastName cannot be null"
  )
  private String lastName;

  /**
   * The email to update
   */
  private String email;

  /**
   * The last update to update
   */
  @NotNull(
      message = "lastUpdate cannot be null"
  )
  private Timestamp lastUpdate;
}
