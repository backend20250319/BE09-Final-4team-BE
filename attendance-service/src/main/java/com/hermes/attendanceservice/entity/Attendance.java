package com.hermes.attendanceservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 출퇴근 아이디 (PK)
  
  @Column(name = "user_id", nullable = false)
  private Long userId; // User 엔티티의 유저 아이디 (FK)
  
  private LocalDate date; // 출퇴근 날짜

  private LocalDateTime checkIn; // 출근 시간
  private LocalDateTime checkOut; // 퇴근 시간

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private WorkStatus status = WorkStatus.NOT_CLOCKIN; // 기본값을 미출근으로 설정
  
  @Builder.Default
  private boolean isAutoRecorded = false; // 수동 기록(버튼)인지, 자동 기록인지
}
