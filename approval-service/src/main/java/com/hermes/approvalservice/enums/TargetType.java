package com.hermes.approvalservice.enums;

public enum TargetType {
    USER("개별 구성원"),
    ORGANIZATION("특정 조직"),
    N_LEVEL_MANAGER("n차 조직장");

    private final String description;

    TargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}