package com.hermes.attendanceservice.entity;

/**
 * 근무 상태를 나타내는 enum */
public enum WorkStatus {
  REGULAR,      // 정상 출근
  LATE,         // 지각
  EARLY_LEAVE,  // 조퇴
  ABSENT,       // 결근
  NOT_CLOCKIN,  // 미출근 DEFAULT
  REMOTE,       // 재택
  BUSINESS_TRIP, // 출장
  OUT_OF_OFFICE, // 외근
  VACATION      // 연차
} 