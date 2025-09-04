package com.hermes.attendanceservice.service.attendance;

import com.hermes.attendanceservice.dto.attendance.AttendanceResponse;
import com.hermes.attendanceservice.dto.attendance.WeeklyWorkSummary;
import com.hermes.attendanceservice.dto.attendance.DailyWorkSummary;
import com.hermes.attendanceservice.entity.attendance.Attendance;
import com.hermes.attendanceservice.entity.attendance.AttendanceStatus;
import com.hermes.attendanceservice.entity.attendance.WorkStatus;
import com.hermes.attendanceservice.repository.attendance.AttendanceRepository;
import com.hermes.attendanceservice.service.workschedule.WorkScheduleService;
import com.hermes.attendanceservice.dto.workschedule.WorkTimeInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@EnableScheduling
public class AttendanceServiceImpl implements AttendanceService {

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    private final AttendanceRepository attendanceRepository;
    private final WorkScheduleService workScheduleService;

    @Override
    public AttendanceResponse checkIn(Long userId, Instant checkInTime) {
        Instant now = Instant.now();
        Instant effective = (checkInTime != null ? checkInTime : now);
        ZonedDateTime zdt = effective.atZone(ZONE_SEOUL);
        LocalDate date = zdt.toLocalDate();

        Attendance a = attendanceRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> Attendance.builder()
                        .userId(userId)
                        .date(date)
                        .attendanceStatus(AttendanceStatus.NOT_CLOCKIN)
                        .workStatus(WorkStatus.OFFICE)
                        .isAutoRecorded(false)
                        .build());

        if (a.getCheckIn() != null) throw new IllegalStateException("이미 출근 처리가 완료되었습니다.");

        a.setCheckIn(effective);

        // WorkSchedule에서 근무 시간 조회
        WorkTimeInfoDto workTime = workScheduleService.getUserWorkTime(userId, date);
        LocalTime scheduledStartTime = workTime.getStartTime();
        
        // 출근 시간과 근무 시작 시간 비교 (30분 전부터 허용)
        LocalTime checkInTimeOnly = zdt.toLocalTime();
        LocalTime allowedStartTime = scheduledStartTime.minusMinutes(30);
        
        // 30분 전보다 일찍 출근한 경우 에러 처리
        if (checkInTimeOnly.isBefore(allowedStartTime)) {
            throw new IllegalStateException("출근 가능 시간이 아닙니다. " + 
                allowedStartTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " 이후에 출근해주세요.");
        }
        
        // 근무 시작 시간보다 늦게 출근한 경우 지각
        if (checkInTimeOnly.isAfter(scheduledStartTime)) {
            a.setAttendanceStatus(AttendanceStatus.LATE);
        } else {
            // 허용 시간 내 출근
            a.setAttendanceStatus(AttendanceStatus.REGULAR);
        }

        a.setAutoRecorded(false);
        return toResponse(attendanceRepository.save(a));
    }

    @Override
    public AttendanceResponse checkOut(Long userId, Instant checkOutTime) {
        Instant now = Instant.now();
        Instant effective = (checkOutTime != null ? checkOutTime : now);
        ZonedDateTime zdt = effective.atZone(ZONE_SEOUL);
        LocalDate date = zdt.toLocalDate();

        Attendance a = attendanceRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new IllegalStateException("출근 기록이 존재하지 않습니다."));

        if (a.getCheckOut() != null) throw new IllegalStateException("이미 퇴근 처리가 완료되었습니다.");

        a.setCheckOut(effective);

        // WorkSchedule에서 근무 종료 시간 조회
        WorkTimeInfoDto workTime = workScheduleService.getUserWorkTime(userId, date);
        LocalTime scheduledEndTime = workTime.getEndTime();
        
        // 퇴근 시간이 근무 종료 시간보다 이른 경우 조퇴
        if (zdt.toLocalTime().isBefore(scheduledEndTime)) {
            a.setWorkStatus(WorkStatus.EARLY_LEAVE);
        }

        return toResponse(attendanceRepository.save(a));
    }

    @Override
    public AttendanceResponse markAttendanceStatus(Long userId,
                                                   LocalDate date,
                                                   AttendanceStatus attendanceStatus,
                                                   boolean autoRecorded,
                                                   Instant checkInTime,
                                                   Instant checkOutTime) {

        Attendance a = attendanceRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> Attendance.builder()
                        .userId(userId)
                        .date(date)
                        .attendanceStatus(AttendanceStatus.NOT_CLOCKIN)
                        .workStatus(WorkStatus.OFFICE)
                        .isAutoRecorded(autoRecorded)
                        .build());

        a.setAttendanceStatus(attendanceStatus);
        if (checkInTime != null)  a.setCheckIn(checkInTime);
        if (checkOutTime != null) a.setCheckOut(checkOutTime);
        a.setAutoRecorded(autoRecorded);

        return toResponse(attendanceRepository.save(a));
    }

    @Override
    public AttendanceResponse markWorkStatus(Long userId,
                                             LocalDate date,
                                             WorkStatus workStatus,
                                             boolean autoRecorded,
                                             Instant checkInTime,
                                             Instant checkOutTime) {

        Attendance a = attendanceRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> Attendance.builder()
                        .userId(userId)
                        .date(date)
                        .attendanceStatus(AttendanceStatus.NOT_CLOCKIN)
                        .workStatus(WorkStatus.OFFICE)
                        .isAutoRecorded(autoRecorded)
                        .build());

        a.setWorkStatus(workStatus);
        if (checkInTime != null)  a.setCheckIn(checkInTime);
        if (checkOutTime != null) a.setCheckOut(checkOutTime);
        a.setAutoRecorded(autoRecorded);

        return toResponse(attendanceRepository.save(a));
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyWorkSummary getThisWeekSummary(Long userId) {
        LocalDate today = LocalDate.now(ZONE_SEOUL);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(SUNDAY));
        return getWeekSummary(userId, weekStart);
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyWorkSummary getWeekSummary(Long userId, LocalDate weekStartSunday) {
        // 입력값이 요일이 아니어도 자동 보정
        LocalDate weekStart = weekStartSunday.with(TemporalAdjusters.previousOrSame(SUNDAY));
        LocalDate weekEnd   = weekStart.with(TemporalAdjusters.nextOrSame(SATURDAY));

        List<Attendance> records = attendanceRepository.findAllByUserIdAndDateBetween(userId, weekStart, weekEnd);

        Map<LocalDate, Long> daily = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) daily.put(weekStart.plusDays(i), 0L);

        long totalMinutes = 0L;
        double regularWorkHours = 0.0;
        double lateWorkHours = 0.0;
        double overtimeHours = 0.0;
        double vacationHours = 0.0;

        for (Attendance a : records) {
            if (a.getCheckIn() == null || a.getCheckOut() == null) continue;

            final long minutes = ChronoUnit.MINUTES.between(a.getCheckIn(), a.getCheckOut());
            final long validMinutes = minutes < 0 ? 0 : minutes;

            daily.computeIfPresent(a.getDate(), (d, m) -> m + validMinutes);
            totalMinutes += validMinutes;

            // 상태별 시간 계산
            double hours = validMinutes / 60.0;
            
            // 출근 상태에 따른 분류
            switch (a.getAttendanceStatus()) {
                case REGULAR:
                    regularWorkHours += hours;
                    break;
                case LATE:
                    lateWorkHours += hours;
                    break;
                default:
                    // 기타 출근 상태는 regularWorkHours에 포함
                    regularWorkHours += hours;
                    break;
            }
            
            // 근무 상태에 따른 추가 분류
            switch (a.getWorkStatus()) {
                case VACATION:
                    vacationHours += hours;
                    break;
                case SICK_LEAVE:
                    vacationHours += hours; // 병가도 휴가 시간으로 계산
                    break;
                default:
                    // 기타 근무 상태는 별도 계산하지 않음
                    break;
            }
        }

        // 초과근무 계산 (주 40시간 기준)
        double totalWorkHours = totalMinutes / 60.0;
        overtimeHours = Math.max(0, totalWorkHours - 40.0);

        return WeeklyWorkSummary.builder()
                .userId(userId)
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .totalWorkMinutes(totalMinutes)
                .totalWorkHours(totalWorkHours)
                .workDays((int) records.stream().filter(a -> a.getCheckIn() != null).count())
                .regularWorkHours(regularWorkHours)
                .lateWorkHours(lateWorkHours)
                .overtimeHours(overtimeHours)
                .vacationHours(vacationHours)
                .dailySummaries(createDailySummaries(records, daily))
                .build();
    }

    @Override
    public Map<String, Object> getCheckInAvailableTime(Long userId) {
        LocalDate today = LocalDate.now(ZONE_SEOUL);
        WorkTimeInfoDto workTime = workScheduleService.getUserWorkTime(userId, today);
        
        LocalTime scheduledStartTime = workTime.getStartTime();
        LocalTime allowedStartTime = scheduledStartTime.minusMinutes(30);
        LocalTime now = LocalTime.now(ZONE_SEOUL);
        
        boolean isAvailable = !now.isBefore(allowedStartTime);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("scheduledStartTime", scheduledStartTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        response.put("allowedStartTime", allowedStartTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        response.put("isAvailable", isAvailable);
        response.put("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm")));
        
        return response;
    }

    private AttendanceResponse toResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .date(a.getDate())
                .checkIn(a.getCheckIn())
                .checkOut(a.getCheckOut())
                .attendanceStatus(a.getAttendanceStatus())
                .workStatus(a.getWorkStatus())
                .autoRecorded(a.isAutoRecorded())
                .build();
    }

    private List<DailyWorkSummary> createDailySummaries(List<Attendance> records, Map<LocalDate, Long> daily) {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        return daily.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    final Long minutes = entry.getValue();
                    
                    Attendance record = records.stream()
                            .filter(r -> r.getDate().equals(date))
                            .findFirst()
                            .orElse(null);
                    
                    String checkInStr = null;
                    String checkOutStr = null;
                    if (record != null && record.getCheckIn() != null) {
                        checkInStr = record.getCheckIn().atZone(ZONE_SEOUL).toLocalTime().format(timeFmt);
                    }
                    if (record != null && record.getCheckOut() != null) {
                        checkOutStr = record.getCheckOut().atZone(ZONE_SEOUL).toLocalTime().format(timeFmt);
                    }
                    
                    return DailyWorkSummary.builder()
                            .date(date)
                            .attendanceStatus(record != null ? record.getAttendanceStatus().name() : "NO_RECORD")
                            .workStatus(record != null ? record.getWorkStatus().name() : "NO_RECORD")
                            .workMinutes(minutes.doubleValue())
                            .workHours(minutes / 60.0)
                            .checkInTime(checkInStr)
                            .checkOutTime(checkOutStr)
                            .workDuration(human(minutes))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String human(long minutes) {
        long h = minutes / 60;
        long m = minutes % 60;
        return h + "시간 " + m + "분";
    }

    // 자동 퇴근 처리 메서드
    @Scheduled(cron = "0 0 0 * * ?") // 매일 24시(자정)에 실행
    public void autoCheckOut() {
        LocalDate today = LocalDate.now(ZONE_SEOUL);
        
        // 출근했지만 퇴근하지 않은 모든 기록 조회
        List<Attendance> incompleteRecords = attendanceRepository
            .findAllByCheckInIsNotNullAndCheckOutIsNullAndDate(today);
        
        for (Attendance attendance : incompleteRecords) {
            try {
                // WorkSchedule에서 근무 종료 시간 조회
                WorkTimeInfoDto workTime = 
                    workScheduleService.getUserWorkTime(attendance.getUserId(), today);
                LocalTime scheduledEndTime = workTime.getEndTime();
                
                // 스케줄된 퇴근 시간으로 자동 퇴근 처리 (Asia/Seoul 기준)
                ZonedDateTime autoZdt = today.atTime(scheduledEndTime).atZone(ZONE_SEOUL);
                attendance.setCheckOut(autoZdt.toInstant());
                attendance.setAutoRecorded(true);
                
                attendanceRepository.save(attendance);
                
                log.info("자동 퇴근 처리: 사용자 {}, 시간: {}", 
                    attendance.getUserId(), autoZdt);
                    
            } catch (Exception e) {
                log.error("자동 퇴근 처리 실패: 사용자 {}", attendance.getUserId(), e);
            }
        }
    }
} 