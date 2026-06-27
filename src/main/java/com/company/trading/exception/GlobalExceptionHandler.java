package com.company.trading.exception;

import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {
  @ExceptionHandler(BadRequestException.class)
  ResponseEntity<ApiError> bad(BadRequestException e) {
    return response(HttpStatus.BAD_REQUEST, e.getMessage(), List.of());
  }

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<ApiError> missing(NotFoundException e) {
    return response(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {
    var details =
        e.getBindingResult().getFieldErrors().stream()
            .map(x -> x.getField() + ": " + x.getDefaultMessage())
            .toList();
    return response(HttpStatus.BAD_REQUEST, "Validation failed", details);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> other(Exception e) {
    return response(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", List.of());
  }

  private ResponseEntity<ApiError> response(
      HttpStatus status, String message, List<String> details) {
    return ResponseEntity.status(status)
        .body(new ApiError(Instant.now(), status.value(), message, details));
  }

  public record ApiError(Instant timestamp, int status, String message, List<String> details) {}
}
