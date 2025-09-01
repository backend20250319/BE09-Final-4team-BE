package com.hermes.attendanceservice.entity.attendance;

/**
 * 출근 상태를 나타내는 enum
 */
public enum AttendanceStatus {
    NOT_CLOCKIN,  // 미출근 (출근 버튼을 누르지 않음)
    REGULAR,      // 정상 출근 (정시 출근)
    LATE,         // 지각 (출근은 했지만 늦게)
    ABSENT        // 결근 (출근하지 않음)
} 