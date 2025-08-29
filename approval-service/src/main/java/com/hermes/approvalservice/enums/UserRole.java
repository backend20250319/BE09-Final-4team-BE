package com.hermes.approvalservice.enums;

public enum UserRole {
    AUTHOR("작성자"),
    APPROVER("승인자"),
    REFERENCE("참조자"),
    VIEWER("조회자");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}