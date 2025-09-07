package com.jfeatures.msg.validname.controller;

import com.jfeatures.msg.validname.dao.ValidNameUpdateDAO;
import com.jfeatures.msg.validname.dto.ValidNameUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.lang.Integer;
import java.lang.String;
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
    name = "ValidName Update API",
    description = "REST API for updating validname records"
)
public class ValidNameUpdateController {
  private final ValidNameUpdateDAO validnameUpdateDAO;

  ValidNameUpdateController(ValidNameUpdateDAO validnameUpdateDAO) {
    this.validnameUpdateDAO = validnameUpdateDAO;
  }

  /**
   * Updates a validname record.
   * @param updateDto The updated data
   * @param id The record identifier
   * @return ResponseEntity indicating success or failure
   */
  @PutMapping(
      value = "/validname/{id}",
      consumes = "application/json"
  )
  @Operation(
      summary = "Update validname record",
      description = "Updates an existing validname record with the provided data"
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Successfully updated"),
          @ApiResponse(responseCode = "400", description = "Invalid request data"),
          @ApiResponse(responseCode = "404", description = "Record not found")
      })
  public ResponseEntity<Void> updateValidName(
      @Valid @RequestBody @Parameter(description = "Updated validname data", required = true) ValidNameUpdateDTO updateDto,
      @PathVariable("id") @Parameter(description = "Unique identifier", required = true) Integer id,
      @RequestParam(value = "status", required = true) @Parameter(description = "Filter parameter: status") String status) {
    int rowsUpdated = validnameUpdateDAO.updateValidName(updateDto, id, status);

    if (rowsUpdated > 0) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
