package com.hermes.attachmentservice.exception;

public class FileUploadException extends RuntimeException {
    
    public FileUploadException(String message) {
        super("파일 업로드에 실패했습니다: " + message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super("파일 업로드에 실패했습니다: " + message, cause);
    }
}