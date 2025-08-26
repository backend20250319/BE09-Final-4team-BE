package com.hermes.attendanceservice.entity.leave;

public enum LeaveType {
    BASIC_ANNUAL("기본휴가", 15),
    COMPENSATION_ANNUAL("보상휴가", 0),
    SPECIAL_ANNUAL("특별휴가", 0);
    
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
