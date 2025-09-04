package com.hermes.communicationservice.common;

import com.hermes.api.common.ApiResult;
import com.hermes.communicationservice.announcement.exception.AnnouncementNotFoundException;
import com.hermes.communicationservice.archive.exception.ArchiveNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AnnouncementNotFoundException.class)
  public ResponseEntity<ApiResult<Void>> handleAnnouncementNotFound(
      AnnouncementNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.failure(ex.getMessage()));
  }

  @ExceptionHandler(ArchiveNotFoundException.class)
  public ResponseEntity<ApiResult<Void>> handleArchiveNotFound(
      ArchiveNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.failure(ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResult<Void>> handleValidation(MethodArgumentNotValidException ex) {
    String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    return ResponseEntity.badRequest().body(ApiResult.failure(msg));
  }

}
