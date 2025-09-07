package com.hermes.attendanceservice.entity.workschedule;

/**
 * 스케줄 타입을 나타내는 enum
 */
public enum ScheduleType {
    WORK("근무"),
    CORETIME("코어타임"),
    SICK_LEAVE("병가"),
    VACATION("휴가"),
    BUSINESS_TRIP("출장"),
    OUT_OF_OFFICE("외근"),
    OVERTIME("초과근무"),
    RESTTIME("휴게시간");
    
    private final String description;
    
    ScheduleType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getValue() {
        return this.name();
    }
} 