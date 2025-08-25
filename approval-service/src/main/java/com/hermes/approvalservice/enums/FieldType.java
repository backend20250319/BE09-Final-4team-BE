package com.hermes.approvalservice.enums;

public enum FieldType {
    TEXT("글자"),
    NUMBER("숫자"),
    MONEY("금액"),
    DATE("날짜"),
    SELECT("선택"),
    MULTISELECT("다중선택");

    private final String description;

    FieldType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}