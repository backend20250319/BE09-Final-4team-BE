package com.hermes.attendanceservice.service;

import com.hermes.attendanceservice.dto.AttendanceResponse;
import com.hermes.attendanceservice.dto.WeeklyWorkSummary;
import com.hermes.attendanceservice.entity.Attendance;
import com.hermes.attendanceservice.entity.WorkStatus;
import com.hermes.attendanceservice.repository.AttendanceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    @Value("${attendance.start-time:09:00}")
    private String startTimeConfig;

    @Value("${attendance.end-time:18:00}")
    private String endTimeConfig;

    @PostConstruct
    public void init() {
        System.out.println("=== DEBUG: endTimeConfig ===");
        System.out.println("endTimeConfig: " + endTimeConfig);
        System.out.println("endTimeConfig length: " + endTimeConfig.length());
        System.out.println("endTimeConfig bytes: " + java.util.Arrays.toString(endTimeConfig.getBytes()));
        System.out.println("=============================");
    }

    private LocalTime startTime() { return LocalTime.parse(startTimeConfig); }
    private LocalTime endTime()   { return LocalTime.parse(endTimeConfig);   }

    @Override
    public AttendanceResponse checkIn(Long userId, LocalDateTime checkInTime) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime effective = (checkInTime != null ? checkInTime : now);
        LocalDate date = effective.toLocalDate();

        Attendance a = attendanceRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> Attendance.builder()
                        .userId(userId)
                        .date(date)
                        .status(WorkStatus.NOT_CLOCKIN)
                        .isAutoRecorded(false)
                        .build());

        if (a.getCheckIn() != null) throw new IllegalStateException("이미 출근 처리된 사용자입니다.");

        a.setCheckIn(effective);

        // 휴가/출장 등은 markStatus로 따로 기록된다고 가정
        if (a.getStatus() == WorkStatus.NOT_CLOCKIN || a.getStatus() == WorkStatus.REGULAR || a.getStatus() == WorkStatus.LATE) {
            a.setStatus(effective.toLocalTime().isAfter(startTime()) ? WorkStatus.LATE : WorkStatus.REGULAR);
        }

        a.setAutoRecorded(false);
        return toResponse(attendanceRepository.save(a));
    }

    @Override
    public AttendanceResponse checkOut(Long userId, LocalDateTime checkOutTime) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime effective = (checkOutTime != null ? checkOutTime : now);
        LocalDate date = effective.toLocalDate();

        Attendance a = attendanceRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new IllegalStateException("출근 기록이 존재하지 않습니다."));

        if (a.getCheckOut() != null) throw new IllegalStateException("이미 퇴근 처리된 사용자입니다.");

        a.setCheckOut(effective);

        if ((a.getStatus() == WorkStatus.REGULAR || a.getStatus() == WorkStatus.LATE)
                && effective.toLocalTime().isBefore(endTime())) {
            a.setStatus(WorkStatus.EARLY_LEAVE);
        }

        return toResponse(attendanceRepository.save(a));
    }

    @Override
    public AttendanceResponse markStatus(Long userId,
                                         LocalDate date,
                                         WorkStatus status,
                                         boolean autoRecorded,
                                         LocalDateTime checkInTime,
                                         LocalDateTime checkOutTime) {

        Attendance a = attendanceRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> Attendance.builder()
                        .userId(userId)
                        .date(date)
                        .status(WorkStatus.NOT_CLOCKIN)
                        .isAutoRecorded(autoRecorded)
                        .build());

        a.setStatus(status);
        if (checkInTime != null)  a.setCheckIn(checkInTime);
        if (checkOutTime != null) a.setCheckOut(checkOutTime);
        a.setAutoRecorded(autoRecorded);

        return toResponse(attendanceRepository.save(a));
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyWorkSummary getThisWeekSummary(Long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(SUNDAY));
        return getWeekSummary(userId, weekStart);
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyWorkSummary getWeekSummary(Long userId, LocalDate weekStartSunday) {
        // 전달된 값이 일요일이 아니어도 자동 보정
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
            switch (a.getStatus()) {
                case REGULAR:
                    regularWorkHours += hours;
                    break;
                case LATE:
                    lateWorkHours += hours;
                    break;
                case VACATION:
                    vacationHours += hours;
                    break;
                default:
                    // 기타 상태는 regularWorkHours에 포함
                    regularWorkHours += hours;
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

    private AttendanceResponse toResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .date(a.getDate())
                .checkIn(a.getCheckIn())
                .checkOut(a.getCheckOut())
                .status(a.getStatus())
                .autoRecorded(a.isAutoRecorded())
                .build();
    }

    private List<WeeklyWorkSummary.DailyWorkSummary> createDailySummaries(List<Attendance> records, Map<LocalDate, Long> daily) {
        return daily.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    final Long minutes = entry.getValue();
                    
                    Attendance record = records.stream()
                            .filter(r -> r.getDate().equals(date))
                            .findFirst()
                            .orElse(null);
                    
                    return WeeklyWorkSummary.DailyWorkSummary.builder()
                            .date(date)
                            .status(record != null ? record.getStatus().name() : "NO_RECORD")
                            .workMinutes(minutes.doubleValue())
                            .workHours(minutes / 60.0)
                            .checkInTime(record != null && record.getCheckIn() != null ? 
                                    record.getCheckIn().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null)
                            .checkOutTime(record != null && record.getCheckOut() != null ? 
                                    record.getCheckOut().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null)
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
}
