package com.hermes.companyinfoservice.entity;

public enum EmployeeCount {
    ONE_TO_TEN("1-10명"),
    ELEVEN_TO_FIFTY("11-50명"),
    FIFTY_TO_HUNDRED("50-100명"),
    HUNDRED_TO_FIVE_HUNDRED("100-500명"),
    OVER_FIVE_HUNDRED("500명 이상");

    private final String displayName;

    EmployeeCount(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 