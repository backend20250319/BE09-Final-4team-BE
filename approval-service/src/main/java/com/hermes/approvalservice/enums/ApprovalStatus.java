package com.hermes.approvalservice.enums;

import lombok.Getter;

@Getter
public enum ApprovalStatus {
    PENDING("대기중"),
    APPROVED("승인함"),
    REJECTED("반려함");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }
}