package com.jfeatures.msg.customer.dto;

import jakarta.validation.constraints.NotNull;
import java.lang.String;
import lombok.Data;

/**
 * DTO for updating customer entity.
 * Contains fields that can be updated via PUT API.
 * Audit fields (updatedAt, updatedBy) are auto-populated and excluded from this DTO.
 */
@Data
public class CustomerUpdateDTO {
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
}
