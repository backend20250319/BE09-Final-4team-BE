package com.hermes.userservice.exception;

// 필수 사용자 헤더가 누락되었을 때 발생하는 예외

public class MissingUserHeaderException extends BusinessException {
    
    public MissingUserHeaderException(String headerName) {
        super("REQUIRED_HEADER_MISSING", 
              String.format("필수 헤더 '%s'가 누락되었습니다.", headerName));
    }
    
    public MissingUserHeaderException(String message, Throwable cause) {
        super("REQUIRED_HEADER_MISSING", message, cause);
    }
}
