package com.hermes.approvalservice.enums;

import lombok.Getter;

@Getter
public enum DocumentStatus {
    DRAFT("임시저장"),
    IN_PROGRESS("진행중"),
    APPROVED("승인됨"),
    REJECTED("반려됨");

    private final String description;

    DocumentStatus(String description) {
        this.description = description;
    }

}