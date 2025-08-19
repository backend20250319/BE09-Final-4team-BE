package com.hermes.attendanceservice.controller;

import com.hermes.attendanceservice.dto.AttendanceResponse;
import com.hermes.attendanceservice.dto.WeeklyWorkSummary;
import com.hermes.attendanceservice.entity.WorkStatus;
import com.hermes.attendanceservice.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public AttendanceResponse checkIn(@RequestParam Long userId,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                      LocalDateTime checkInTime) {
        return attendanceService.checkIn(userId, checkInTime);
    }

    @PostMapping("/check-out")
    public AttendanceResponse checkOut(@RequestParam Long userId,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                       LocalDateTime checkOutTime) {
        return attendanceService.checkOut(userId, checkOutTime);
    }

    /** 연차/출장/외근/재택 등 상태 기록 */
    @PostMapping("/status")
    public AttendanceResponse markStatus(@RequestParam Long userId,
                                         @RequestParam
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                         LocalDate date,
                                         @RequestParam WorkStatus status,
                                         @RequestParam(defaultValue = "true") boolean autoRecorded,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                         LocalDateTime checkInTime,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                         LocalDateTime checkOutTime) {
        return attendanceService.markStatus(userId, date, status, autoRecorded, checkInTime, checkOutTime);
    }

    /** 이번 주(일~토) 요약 */
    @GetMapping("/weekly/this")
    public WeeklyWorkSummary getThisWeek(@RequestParam Long userId) {
        return attendanceService.getThisWeekSummary(userId);
    }

    /** 임의 주(weekStart가 일요일이 아니어도 자동 보정) */
    @GetMapping("/weekly")
    public WeeklyWorkSummary getWeek(@RequestParam Long userId,
                                     @RequestParam
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                     LocalDate weekStart) {
        return attendanceService.getWeekSummary(userId, weekStart);
    }
}
