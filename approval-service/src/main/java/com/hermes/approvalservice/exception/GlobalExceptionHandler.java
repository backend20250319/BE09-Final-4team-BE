package com.hermes.approvalservice.exception;

import com.hermes.approvalservice.dto.ApiResponse;
import com.hermes.jwt.context.AuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NotFoundException e) {
        log.warn("Not found exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("Unauthorized exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.rejected(e.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(e.getMessage()));
    }

    @ExceptionHandler(AuthContext.AuthenticationRequiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationRequiredException(AuthContext.AuthenticationRequiredException e) {
        log.warn("Authentication required: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.rejected(e.getMessage()));
    }

    @ExceptionHandler(AuthContext.InsufficientPermissionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientPermissionException(AuthContext.InsufficientPermissionException e) {
        log.warn("Insufficient permission: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.rejected(e.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception e) {
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
                .body(ApiResponse.failure(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("서버 오류가 발생했습니다."));
    }
}