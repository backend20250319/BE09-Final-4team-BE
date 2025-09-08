package com.hermes.approvalservice.enums;

import lombok.Getter;

@Getter
public enum DocumentRole {
    AUTHOR("작성자"),
    APPROVER("승인자"),
    REFERENCE("참조자"),
    VIEWER("조회자");

    private final String description;

    DocumentRole(String description) {
        this.description = description;
    }

}