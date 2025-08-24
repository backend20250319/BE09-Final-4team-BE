package com.hermes.approvalservice.enums;

public enum DocumentStatus {
    DRAFT("임시저장"),
    PENDING("승인대기"),
    IN_PROGRESS("승인중"),
    APPROVED("승인완료"),
    REJECTED("반려");

    private final String description;

    DocumentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}