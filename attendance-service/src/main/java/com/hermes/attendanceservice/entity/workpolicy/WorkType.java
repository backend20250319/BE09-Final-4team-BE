package com.hermes.attendanceservice.entity.workpolicy;

/**
 * 근무 유형을 나타내는 enum
 */
public enum WorkType {
    FIXED("고정"),           // 고정 근무
    SHIFT("교대"),           // 교대 근무
    FLEXIBLE("시차"),        // 시차 근무 (출근 가능 시간대 설정 가능)
    OPTIONAL("선택");        // 선택 근무
    
    private final String name;
    
    WorkType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
} 
