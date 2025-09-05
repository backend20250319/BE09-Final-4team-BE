package com.hermes.approvalservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
        log.warn("Not found exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("Unauthorized exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }


    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<String> handleValidationException(Exception e) {
        String message = "입력값이 올바르지 않습니다.";
        
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            if (ex.getBindingResult().hasFieldErrors()) {
                message = ex.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            if (ex.getBindingResult().hasFieldErrors()) {
                message = ex.getBindingResult().getFieldError().getDefaultMessage();
            }
        }
        
        log.warn("Validation exception: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String paramName = e.getName();
        String message = String.format("파라미터 '%s'의 값이 올바르지 않습니다. 올바른 형식을 확인해주세요.", paramName);
        
        log.warn("Method argument type mismatch exception: parameter={}, value={}, requiredType={}", 
                paramName, e.getValue(), e.getRequiredType());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.";
        
        log.warn("HTTP message not readable exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 오류가 발생했습니다.");
    }
}