package com.hermes.tenantservice.exception;

import com.hermes.tenantservice.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 * 전역 예외 처리 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 유효성 검증 실패 (@Valid 어노테이션)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleValidationException(
            MethodArgumentNotValidException e) {
        
        log.warn("Validation failed: {}", e.getMessage());
        
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(String.format("%s: %s", error.getField(), error.getDefaultMessage()));
        }
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("입력 데이터 검증에 실패했습니다", errors));
    }

    /**
     * 바인딩 예외
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleBindException(BindException e) {
        log.warn("Binding failed: {}", e.getMessage());
        
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(String.format("%s: %s", error.getField(), error.getDefaultMessage()));
        }
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("데이터 바인딩에 실패했습니다", errors));
    }

    /**
     * 제약 조건 위반
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleConstraintViolationException(
            ConstraintViolationException e) {
        
        log.warn("Constraint violation: {}", e.getMessage());
        
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            errors.add(String.format("%s: %s", 
                violation.getPropertyPath(), 
                violation.getMessage()));
        }
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("제약 조건 위반", errors));
    }

    /**
     * 메서드 인자 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        
        log.warn("Type mismatch: parameter={}, value={}, requiredType={}", 
                e.getName(), e.getValue(), e.getRequiredType().getSimpleName());
        
        String message = String.format("잘못된 파라미터 타입입니다. %s는 %s 타입이어야 합니다", 
                e.getName(), e.getRequiredType().getSimpleName());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, "TYPE_MISMATCH"));
    }

    /**
     * IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e) {
        
        log.warn("Illegal argument: {}", e.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), "INVALID_ARGUMENT"));
    }

    /**
     * IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException e) {
        
        log.error("Illegal state: {}", e.getMessage(), e);
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage(), "INVALID_STATE"));
    }

    /**
     * RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred", e);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다: " + e.getMessage(), "RUNTIME_ERROR"));
    }

    /**
     * 기타 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred", e);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("알 수 없는 오류가 발생했습니다", "UNKNOWN_ERROR"));
    }
}
