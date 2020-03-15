package com.sourceclear.agile.piplanning.service.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandlers {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final ObjectMapper objectMapper;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  public ExceptionHandlers(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<String> badCredentialsException(Exception ex) throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(buildJsonStringMessage("Unauthorized", ex.getMessage()));
  }

  /**
   * Mainly to deal with the fact that we use FreeBuilder for our API requests and if any compulsory fields
   * are missing, FreeBuilder's exception will result in a 500 Internal Server Error when it is a 400 Bad Request.
   */
  @ExceptionHandler(HttpMessageConversionException.class)
  public ResponseEntity<String> httpMessageConversionException(Exception ex) throws Exception {
    return ResponseEntity.badRequest()
        .contentType(MediaType.APPLICATION_JSON)
        .body(buildJsonStringMessage("Bad Request", ex.getMessage()));
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  private String buildJsonStringMessage(String error, String message) throws IOException {
    final Map<String, String> response = Map.of("error", error, "message", message);
    return objectMapper.writeValueAsString(response);
  }

  //---------------------------- Getters/Setters ------------------------------

}
