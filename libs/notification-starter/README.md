# notification-starter

Hermes 프로젝트의 알림 기능을 위한 Spring Boot Starter입니다. RabbitMQ를 통한 비동기 알림 발송 기능을 제공합니다.

## 기능

- **비동기 알림 발송**: RabbitMQ를 통한 비동기 알림 처리
- **자동 설정**: Spring Boot Auto Configuration을 통한 자동 빈 등록
- **배치 알림**: 여러 사용자에게 동시 알림 발송
- **실패 처리**: 개별 알림 실패 시 로깅 및 예외 처리
- **알림 타입 지원**: 공지사항, 결재 관련 알림 등 다양한 타입 지원

## 의존성 추가

### build.gradle
```gradle
dependencies {
    implementation project(':libs:notification-starter')
}
```

## 설정

### application.yml
```yaml
# 기본 설정 (필수)
hermes:
  notification:
    enabled: true                           # 알림 기능 활성화



## 사용법

### 1. NotificationPublisher 주입
```java
@Service
@RequiredArgsConstructor
public class AnnouncementService {
    
    private final NotificationPublisher notificationPublisher;
    
    // 서비스 로직...
}
```

### 2. 알림 발송
```java
// 공지사항 알림 발송 예제
@Transactional
public void publishAnnouncement(Announcement announcement) {
    // 공지사항 저장 로직...
    
    // 전체 사용자 ID 조회
    List<Long> allUserIds = userService.getAllUserIds();
    
    // 알림 요청 생성
    NotificationRequest request = NotificationRequest.builder()
        .userIds(allUserIds)
        .type(NotificationType.ANNOUNCEMENT)
        .content(announcement.getTitle()) // 알림 내용을 작성합니다.
        .referenceId(announcement.getId())
        .createdAt(LocalDateTime.now())
        .build();
    
    // 알림 발송
    try {
        NotificationResponse response = notificationPublisher.publish(request);
        log.info("공지사항 알림 발송 완료: 성공 {}/{}", 
            response.getSuccessCount(), response.getTotalCount());
    } catch (NotificationPublishException e) {
        log.error("공지사항 알림 발송 실패: {}", e.getMessage());
        // 필요 시 추가 처리...
    }
}
```

### 3. 결재 알림 발송 예제
```java
// 결재 요청 알림
public void sendApprovalRequest(Document document, List<Long> approverIds) {
    NotificationRequest request = NotificationRequest.builder()
        .userIds(approverIds)
        .type(NotificationType.APPROVAL_REQUEST)
        .content(document.getTitle() + " 결재 요청") // 알림 내용을 작성합니다.
        .referenceId(document.getId())
        .createdAt(LocalDateTime.now())
        .build();
    
    notificationPublisher.publish(request);
}

// 결재 승인 알림
public void sendApprovalApproved(Document document, Long requesterId) {
    NotificationRequest request = NotificationRequest.builder()
        .userIds(List.of(requesterId))
        .type(NotificationType.APPROVAL_APPROVED)
        .content(document.getTitle() + " 결재 승인됨") // 알림 내용을 작성합니다.
        .referenceId(document.getId())
        .createdAt(LocalDateTime.now())
        .build();
    
    notificationPublisher.publish(request);
}
```

## 알림 타입

```java
public enum NotificationType {
    ANNOUNCEMENT,        // 공지사항
    APPROVAL_REQUEST,    // 결재 요청
    APPROVAL_APPROVED,   // 결재 승인
    APPROVAL_REJECTED,   // 결재 반려
    APPROVAL_REFERENCE   // 결재 참조
}
```

## 응답 처리

### NotificationResponse 활용
```java
NotificationResponse response = notificationPublisher.publish(request);

// 성공률 확인
if (response.isCompleteSuccess()) {
    log.info("모든 사용자에게 알림 발송 성공");
} else if (response.isCompleteFailure()) {
    log.error("모든 사용자 알림 발송 실패");
    // 완전 실패 시 NotificationPublishException 발생
} else {
    log.warn("부분 성공: {}/{} 성공", response.getSuccessCount(), response.getTotalCount());
    // 실패한 사용자 정보는 로그에서 확인 가능
}
```



## 주의사항

1. **RabbitMQ 연결**: RabbitMQ 서버가 실행 중이어야 합니다.
2. **예외 처리**: 완전 실패 시 `NotificationPublishException` 발생
3. **로깅**: 실패한 사용자 정보는 WARN/ERROR 레벨로 자동 로깅됩니다.
4. **트랜잭션**: 알림 발송은 비동기이므로 트랜잭션과 별도로 처리됩니다.

## 개발 시 참고


### 커스텀 설정 (필요시)
```java
@Configuration
public class CustomNotificationConfig {
    
    @Bean
    @Primary
    public NotificationSender customNotificationSender() {
        // 커스텀 구현체 등록
        return new CustomNotificationSender();
    }
}
```