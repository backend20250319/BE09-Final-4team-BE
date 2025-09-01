package com.hermes.attendanceservice.entity.attendance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.hermes.attendanceservice.entity.attendance.AttendanceStatus;
import com.hermes.attendanceservice.entity.attendance.WorkStatus;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 출퇴근 기록의 고유 식별자(PK)
  
  @Column(name = "user_id", nullable = false)
  private Long userId; // User 엔티티의 고유 식별자(FK)
  
  private LocalDate date; // 출퇴근 날짜

  private LocalDateTime checkIn; // 출근 시간
  private LocalDateTime checkOut; // 퇴근 시간

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private AttendanceStatus attendanceStatus = AttendanceStatus.NOT_CLOCKIN; // 출근 상태
  
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private WorkStatus workStatus = WorkStatus.OFFICE; // 근무 상태
  
  @Builder.Default
  private boolean isAutoRecorded = false; // 자동 기록(버튼)인지, 수동 기록인지
} 
