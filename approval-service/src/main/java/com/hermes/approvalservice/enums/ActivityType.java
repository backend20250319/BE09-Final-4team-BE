package com.hermes.approvalservice.enums;

public enum ActivityType {
    CREATE("작성"),
    UPDATE("수정"),
    SUBMIT("결재요청"),
    APPROVE("승인"),
    REJECT("반려"),
    MODIFY_APPROVAL("승인대상변경"),
    COMMENT("댓글"),
    DELETE("삭제");

    private final String description;

    ActivityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}