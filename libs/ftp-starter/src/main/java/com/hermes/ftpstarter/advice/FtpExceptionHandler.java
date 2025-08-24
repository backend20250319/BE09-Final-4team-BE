package com.hermes.ftpstarter.advice;

import com.hermes.api.common.ApiResponse;
import com.hermes.ftpstarter.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FtpExceptionHandler {

  @ExceptionHandler(FtpFileNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleNotFound(FtpFileNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.failure(ex.getMessage()));
  }

  @ExceptionHandler(FtpUploadException.class)
  public ResponseEntity<ApiResponse<Object>> handleUpload(FtpUploadException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.failure(ex.getMessage()));
  }

  @ExceptionHandler(FtpDeleteException.class)
  public ResponseEntity<ApiResponse<Object>> handleDelete(FtpDeleteException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.failure(ex.getMessage()));
  }
}

