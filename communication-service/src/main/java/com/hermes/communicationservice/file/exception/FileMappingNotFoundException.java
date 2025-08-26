package com.hermes.communicationservice.file.exception;

public class FileMappingNotFoundException extends RuntimeException {
    public FileMappingNotFoundException(Long id) {
        super("존재하지 않는 파일입니다. id=" + id);
    }
}

