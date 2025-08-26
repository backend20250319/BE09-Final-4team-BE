package com.hermes.attendanceservice.entity.workpolicy;

/**
 * 근무 시작 요일을 나타내는 enum
 * 근무 주기의 시작 요일 (월요일)
 */
public enum StartDayOfWeek {
    MONDAY("월요일", 1),
    TUESDAY("화요일", 2),
    WEDNESDAY("수요일", 3),
    THURSDAY("목요일", 4),
    FRIDAY("금요일", 5),
    SATURDAY("토요일", 6),
    SUNDAY("일요일", 7);
    
    private final String name;
    private final int dayOfWeek;
    
    StartDayOfWeek(String name, int dayOfWeek) {
        this.name = name;
        this.dayOfWeek = dayOfWeek;
    }
    
    public String getName() {
        return name;
    }
    
    public int getDayOfWeek() {
        return dayOfWeek;
    }
    
    /**
     * Java의 DayOfWeek로 매핑
     * @return java.time.DayOfWeek 객체
     */
    public java.time.DayOfWeek toJavaDayOfWeek() {
        return java.time.DayOfWeek.of(dayOfWeek);
    }
    
    /**
     * 기본값을 반환 (월요일)
     * @return MONDAY
     */
    public static StartDayOfWeek getDefault() {
        return MONDAY;
    }
} 
