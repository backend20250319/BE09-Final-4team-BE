package com.hermes.attachmentservice.exception;

public class FileStorageException extends RuntimeException {
    
    public FileStorageException(String message) {
        super("파일 저장소 오류: " + message);
    }
    
    public FileStorageException(String message, Throwable cause) {
        super("파일 저장소 오류: " + message, cause);
    }
}