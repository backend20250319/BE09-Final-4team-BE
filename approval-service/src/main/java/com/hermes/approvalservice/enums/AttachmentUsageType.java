package com.hermes.approvalservice.enums;

public enum AttachmentUsageType {
    DISABLED("사용안함"),
    OPTIONAL("선택사용"),
    REQUIRED("필수");

    private final String description;

    AttachmentUsageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}