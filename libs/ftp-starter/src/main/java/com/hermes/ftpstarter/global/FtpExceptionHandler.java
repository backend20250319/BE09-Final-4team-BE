package com.hermes.ftpstarter.global;

import com.hermes.api.common.ApiResult;
import com.hermes.ftpstarter.exception.FtpDeleteException;
import com.hermes.ftpstarter.exception.FtpEmptyFileException;
import com.hermes.ftpstarter.exception.FtpFileNotFoundException;
import com.hermes.ftpstarter.exception.FtpUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FtpExceptionHandler {

  // 업로드할 파일이 비었을 때 → 400 Bad Request
  @ExceptionHandler(FtpEmptyFileException.class)
  public ResponseEntity<ApiResult<Object>> handleEmptyFile(FtpEmptyFileException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResult.failure(ex.getMessage()));
  }

  // 업로드 실패 → 500 Internal Server Error
  @ExceptionHandler(FtpUploadException.class)
  public ResponseEntity<ApiResult<Object>> handleUpload(FtpUploadException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResult.failure(ex.getMessage()));
  }

  // 삭제 실패 → 500 Internal Server Error
  @ExceptionHandler(FtpDeleteException.class)
  public ResponseEntity<ApiResult<Object>> handleDelete(FtpDeleteException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResult.failure(ex.getMessage()));
  }

  // 파일이 존재하지 않을 때 → 404 Not Found
  @ExceptionHandler(FtpFileNotFoundException.class)
  public ResponseEntity<ApiResult<Object>> handleNotFound(FtpFileNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResult.failure(ex.getMessage()));
  }
}
