package com.hermes.communicationservice.gloabl;

import com.hermes.api.common.ApiResult;
import com.hermes.communicationservice.ftp.exception.FileMappingNotFoundException;
import com.hermes.communicationservice.ftp.exception.FileMappingSaveException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(FileMappingNotFoundException.class)
  public ResponseEntity<ApiResult<Object>> handleFileNotFound(FileMappingNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResult.failure(e.getMessage()));
  }

  @ExceptionHandler(FileMappingSaveException.class)
  public ResponseEntity<ApiResult<Object>> handleFileMappingSave(FileMappingSaveException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResult.failure(e.getMessage()));
  }

  // 개별 파일 혹은 요청 전체 용량 초과 시
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResult<Object>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
    String message = "업로드 실패: ";
    if (ex.getMessage().contains("individual")) {
      message += "파일 하나당 최대 10MB까지 업로드 가능합니다.";
    } else {
      message += "모든 파일 합계가 20MB를 초과했습니다.";
    }

    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(ApiResult.failure(message));
  }

//  @ExceptionHandler(FtpException.class)
//  public void handleFtpException(FtpException e) {
//    throw e; // 그대로 던져서 FtpExceptionHanlder에게 위임
//  }


}
