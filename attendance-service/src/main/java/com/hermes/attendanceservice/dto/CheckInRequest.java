package com.hermes.attendanceservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CheckInRequest {

    private Long userId;              // 출근하는 사용자 ID
    private LocalDateTime checkIn;    // 출근 시간 (보통 서버에서 now()로 처리 가능)

}
