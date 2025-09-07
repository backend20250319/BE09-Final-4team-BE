# Attendance Service - Work Monitor Integration

## 개요

출석 관리 서비스에서 관리자용 대시보드에 실시간 근무 현황을 제공하는 Work Monitor 기능과 자동 퇴근 처리 기능을 구현했습니다.

## 주요 기능

### 1. 실시간 근무 모니터링

- **정상 출근 인원**: 근무 시간 내에 체크인한 직원 수
- **지각 인원**: 근무 시간 이후에 체크인한 직원 수
- **휴가 인원**: 승인된 휴가 신청자 수

### 2. 자동 퇴근 처리 시스템 ⭐ NEW

- **자동 실행**: 매일 자정(00:00)에 자동으로 미완료 출근 기록 처리
- **근무표 기반**: 각 직원의 근무표 종료 시간(endTime)으로 자동 퇴근 처리
- **전날 처리**: 전날 미처리된 기록도 함께 처리

### 3. 날짜 기반 데이터 관리

- 날짜를 Primary Key로 하여 일별 데이터 관리
- 새로운 날짜마다 새로운 행(row) 생성
- 기존 데이터 삭제 없이 히스토리 보존

### 4. 자동 갱신 시스템

- **실시간 갱신**: 출근/퇴근 시 즉시 데이터 업데이트
- **정기 갱신**: 30분마다 자동 데이터 새로고침
- **일일 갱신**: 자정에 전날 데이터 최종 집계 및 오늘 데이터 초기화

### 5. 코어타임 시스템 ⭐ NEW

- **유연한 적용**: 모든 근무정책에서 코어타임 설정 가능 (선택사항)
- **필수 근무시간**: 모든 직원이 반드시 근무해야 하는 시간대 설정
- **시각적 구분**: 캘린더에서 일반 근무시간과 다른 색상으로 표시
- **자동 스케줄 생성**: 근무정책 적용 시 코어타임 블록 자동 생성

## API 엔드포인트

### 출석 관리 API

- **POST /api/attendance/check-in**: 출근 체크인
- **POST /api/attendance/check-out**: 퇴근 체크아웃
- **GET /api/attendance/check-in-available-time**: 출근 가능 시간 조회

### 근무 모니터링 API

- **GET /api/work-monitor/today**: 오늘 날짜의 근무 모니터링 데이터 조회
- **GET /api/work-monitor/{date}**: 특정 날짜의 근무 모니터링 데이터 조회
- **POST /api/work-monitor/update/today**: 오늘 데이터 강제 갱신
- **POST /api/work-monitor/update/{date}**: 특정 날짜 데이터 강제 갱신

## 자동 퇴근 처리 로직

### 실행 시점

- **자동 실행**: 매일 자정(00:00) - `@Scheduled(cron = "0 0 0 * * ?")`

### 처리 대상

- 출근(checkIn)은 했지만 퇴근(checkOut)하지 않은 모든 기록
- 오늘과 어제 날짜의 미완료 기록 모두 처리

### 처리 방식

1. 각 직원의 근무표(WorkSchedule)에서 종료 시간(endTime) 조회
2. 해당 시간으로 자동 퇴근 처리 (`autoRecorded = true`)
3. 조퇴 상태는 적용하지 않음 (정상 퇴근으로 처리)
4. 처리 완료 후 WorkMonitor 데이터 자동 갱신

### 에러 처리

- 개별 직원 처리 실패 시에도 다른 직원들은 계속 처리
- 상세한 로그 기록으로 문제 추적 가능

## 데이터베이스 스키마

```sql
CREATE TABLE work_monitor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date DATE NOT NULL UNIQUE,
    total_employees INTEGER NOT NULL,
    attendance_count INTEGER NOT NULL DEFAULT 0,
    late_count INTEGER NOT NULL DEFAULT 0,
    vacation_count INTEGER NOT NULL DEFAULT 0
);
```

## 출근 상태 판별 로직

### 정상 출근 (REGULAR)

- 근무 시작 시간 30분 전 ~ 근무 시작 시간까지 체크인

### 지각 (LATE)

- 근무 시작 시간 이후 체크인

### 출근 불가

- 근무 시작 시간 30분 전보다 일찍 체크인 시도 시 에러

## 프론트엔드 연동

### 자동 갱신

- 30초마다 데이터 자동 새로고침
- 출근/퇴근 버튼 클릭 시 즉시 갱신
- 날짜 변경 시 자동으로 새 날짜 데이터 로드

### 에러 처리

- API 호출 실패 시 기본값(0) 표시
- 상세한 에러 로깅 및 사용자 알림

## 스케줄링 작업

### @Scheduled 태스크

- **dailyWorkMonitorUpdate()**: 매일 자정 실행, 전날 데이터 최종 집계
- **periodicWorkMonitorUpdate()**: 30분마다 실행, 실시간 데이터 갱신

### 이벤트 기반 갱신

- AttendanceService의 checkIn/checkOut 후 자동 호출
- 실시간성 보장

## 스케줄 타입

### 기본 스케줄 타입

- **WORK**: 일반 근무시간
- **CORETIME**: 코어타임 (선택근무 시 필수 근무시간) ⭐ NEW
- **RESTTIME**: 휴게시간
- **SICK_LEAVE**: 병가
- **VACATION**: 휴가
- **BUSINESS_TRIP**: 출장
- **OUT_OF_OFFICE**: 외근
- **OVERTIME**: 초과근무

### 코어타임 특징

- 코어타임 시작/종료 시간이 설정된 경우에만 생성
- 캘린더에서 진한 초록색(#28A745)으로 표시하여 다른 스케줄과 명확히 구분
- `coreTimeStart`와 `coreTimeEnd` 필드로 시간 설정
- 모든 직원이 반드시 근무해야 하는 시간대를 나타냄
- 근무정책 타입에 관계없이 선택적으로 사용 가능

### 스케줄 타입별 색상 구분

- **WORK**: 파란색 (#3B82F6) - 기본 근무
- **CORETIME**: 진한 초록색 (#28A745) - 코어타임
- **RESTTIME**: 노란색 (#FFC107) - 휴게시간
- **SICK_LEAVE**: 빨간색 (#DC3545) - 병가
- **VACATION**: 주황색 (#FD7E14) - 휴가
- **BUSINESS_TRIP**: 보라색 (#6F42C1) - 출장
- **OUT_OF_OFFICE**: 청록색 (#20C997) - 외근
- **OVERTIME**: 핑크색 (#E83E8C) - 초과근무
- **REMOTE**: 회색 (#6C757D) - 재택근무

## 사용 방법

1. 관리자로 로그인하여 대시보드 접속
2. 상단의 출근/지각/휴가 카드에서 실시간 현황 확인
3. 날짜가 바뀌면 자동으로 새로운 날짜의 데이터로 초기화
4. 출근/퇴근 버튼 클릭 시 즉시 통계 반영
