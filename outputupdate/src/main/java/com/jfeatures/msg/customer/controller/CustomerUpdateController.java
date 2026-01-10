package com.jfeatures.msg.customer.controller;

import com.jfeatures.msg.customer.dao.CustomerUpdateDAO;
import com.jfeatures.msg.customer.dto.CustomerUpdateDTO;
import com.jfeatures.msg.customer.util.CustomerAuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.lang.Integer;
import java.lang.String;
import java.lang.System;
import java.lang.Void;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "/api"
)
@Tag(
    name = "Customer Update API",
    description = "REST API for updating customer records"
)
public class CustomerUpdateController {
  private final CustomerUpdateDAO customerUpdateDAO;

  CustomerUpdateController(CustomerUpdateDAO customerUpdateDAO) {
    this.customerUpdateDAO = customerUpdateDAO;
  }

  /**
   * Updates a customer record.
   * @param updateDto The updated data
   * @param id The record identifier
   * @return ResponseEntity indicating success or failure
   */
  @PutMapping(
      value = "/customer/{id}",
      consumes = "application/json"
  )
  @Operation(
      summary = "Update customer record",
      description = "Updates an existing customer record with the provided data"
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Successfully updated"),
          @ApiResponse(responseCode = "400", description = "Invalid request data"),
          @ApiResponse(responseCode = "404", description = "Record not found")
      })
  public ResponseEntity<Void> updateCustomer(
      @Valid @RequestBody @Parameter(description = "Updated customer data", required = true) CustomerUpdateDTO updateDto,
      @PathVariable("id") @Parameter(description = "Unique identifier", required = true) Integer id,
      @RequestParam(value = "status", required = true) @Parameter(description = "Filter parameter: status") String status,
      HttpServletRequest request) {
    String userId = CustomerAuthenticationUtil.getUserId(request);
    System.out.println("User updating customer: " + userId);

    int rowsUpdated = customerUpdateDAO.updateCustomer(updateDto, id, status, request);

    if (rowsUpdated > 0) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
