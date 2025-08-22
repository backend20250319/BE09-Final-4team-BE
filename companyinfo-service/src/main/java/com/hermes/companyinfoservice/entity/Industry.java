package com.hermes.companyinfoservice.entity;

public enum Industry {
    IT_SW("IT/SW"),
    MANUFACTURING("제조업"),
    SERVICE("서비스업"),
    FINANCE("금융업"),
    MEDICAL_PHARMA("의료/제약"),
    OTHER("기타");

    private final String displayName;

    Industry(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 