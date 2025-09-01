package com.hermes.attachmentservice.exception;

public class FileNotFoundException extends RuntimeException {
    
    public FileNotFoundException(String fileId) {
        super("파일을 찾을 수 없습니다: " + fileId);
    }
    
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}