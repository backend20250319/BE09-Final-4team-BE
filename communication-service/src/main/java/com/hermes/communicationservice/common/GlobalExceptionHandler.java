package com.hermes.communicationservice.common;

import com.hermes.api.common.ApiResult;
import com.hermes.communicationservice.announcement.exception.AnnouncementNotFoundException;
import com.hermes.communicationservice.file.exception.FileMappingNotFoundException;
import com.hermes.communicationservice.file.exception.FileMappingSaveException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AnnouncementNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleFileMappingNotFound(AnnouncementNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.failure(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(ApiResult.failure(msg));
    }

    @ExceptionHandler(FileMappingNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleFileMappingNotFound(FileMappingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.failure(ex.getMessage()));
    }

    @ExceptionHandler(FileMappingSaveException.class)
    public ResponseEntity<ApiResult<Void>> handleFileMappingSave(FileMappingSaveException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.failure(ex.getMessage()));
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
}
