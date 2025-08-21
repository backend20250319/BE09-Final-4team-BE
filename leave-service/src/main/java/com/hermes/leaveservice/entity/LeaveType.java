package com.hermes.leaveservice.entity;

public enum LeaveType {
    BASIC_ANNUAL("기본연차", 15),
    COMPENSATION_ANNUAL("보상연차", 0),
    SPECIAL_ANNUAL("특별연차", 0);
    
    private final String name;
    private final Integer defaultDays;
    
    LeaveType(String name, Integer defaultDays) {
        this.name = name;
        this.defaultDays = defaultDays;
    }
    
    public String getName() {
        return name;
    }
    
    public Integer getDefaultDays() {
        return defaultDays;
    }
} 