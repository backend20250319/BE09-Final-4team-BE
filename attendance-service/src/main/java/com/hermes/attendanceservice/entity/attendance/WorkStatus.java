package com.hermes.attendanceservice.entity.attendance;

/**
 * 근무 상태를 나타내는 enum
 */
public enum WorkStatus {
    OFFICE,        // 사무실 근무
    REMOTE,        // 재택 근무
    BUSINESS_TRIP, // 출장
    OUT_OF_OFFICE, // 외근
    VACATION,      // 휴가
    SICK_LEAVE,    // 병가
    EARLY_LEAVE    // 조퇴
} 
