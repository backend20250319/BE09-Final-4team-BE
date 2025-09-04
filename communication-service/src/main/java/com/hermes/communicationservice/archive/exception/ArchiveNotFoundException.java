package com.hermes.communicationservice.archive.exception;

public class ArchiveNotFoundException extends RuntimeException {
    public ArchiveNotFoundException(Long id) {
        super("존재하지 않는 사내 문서입니다. id=" + id);
    }
}