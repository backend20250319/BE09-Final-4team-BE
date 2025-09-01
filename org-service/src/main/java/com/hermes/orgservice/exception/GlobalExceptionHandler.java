package com.hermes.orgservice.exception;

import com.hermes.api.common.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrganizationNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleOrganizationNotFound(OrganizationNotFoundException ex) {
        log.warn("조직을 찾을 수 없음: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResult.failure(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateOrganizationException.class)
    public ResponseEntity<ApiResult<Void>> handleDuplicateOrganization(DuplicateOrganizationException ex) {
        log.warn("중복 조직: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResult.failure(ex.getMessage()));
    }

    @ExceptionHandler(EmployeeAssignmentNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleEmployeeAssignmentNotFound(EmployeeAssignmentNotFoundException ex) {
        log.warn("직원 배정을 찾을 수 없음: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResult.failure(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("유효성 검사 실패: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResult.failure("입력 데이터가 유효하지 않습니다.", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception ex) {
        log.error("서버 내부 오류: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.failure("서버 내부 오류가 발생했습니다."));
    }
}
