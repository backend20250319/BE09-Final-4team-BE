# Attachment Client Starter 공통 모듈 제안

approval-service 개발 중에 새로운 아키텍처를 기반으로 첨부파일 기능을 구현하면서, 다른 서비스에서도 동일한 패턴으로 사용할 수 있을 것 같다는 생각이 들었습니다.

매우 범용적이고 재사용 가능한 구조로 구현했기 때문에 공통 라이브러리로 분리하면 좋을 것 같습니다.

## 현재 approval-service의 첨부파일 구조

### 주요 컴포넌트들

1. **핵심 엔터티**
    - `AttachmentInfo` - JPA `@Embeddable` 첨부파일 정보

2. **DTO 클래스들**
    - `AttachmentInfoRequest` - 첨부파일 요청 DTO
    - `AttachmentInfoResponse` - 첨부파일 응답 DTO
    - `AttachmentMetadata` - attachment-service로부터 받는 메타데이터

3. **서비스 연동**
    - `AttachmentServiceClient` - attachment-service 호출용 Feign 클라이언트
    - `AttachmentServiceClientFallback` - Circuit Breaker 패턴 구현
    - `AttachmentService` - 첨부파일 검증/변환 비즈니스 로직

### 사용 패턴

```java
// 1. Entity에서 사용
@Entity
public class ApprovalDocument {
    @ElementCollection
    @CollectionTable(name = "document_attachments", joinColumns = @JoinColumn(name = "document_id"))
    private List<AttachmentInfo> attachments = new ArrayList<>();
}

// 2. Service에서 검증 및 변환
List<AttachmentInfo> attachments = attachmentService.validateAndConvertAttachments(request.getAttachments());

// 3. Response 변환
response.setAttachments(attachmentService.convertToResponseList(document.getAttachments()));
```

## 제안: attachment-client-starter 분리

### 새로운 라이브러리 구조

```
libs/
└── attachment-client-starter/
    ├── src/main/java/com/hermes/attachment/
    │   ├── entity/
    │   │   └── AttachmentInfo.java                    # JPA Embeddable
    │   ├── dto/
    │   │   ├── AttachmentInfoRequest.java             # 요청 DTO
    │   │   ├── AttachmentInfoResponse.java            # 응답 DTO
    │   │   └── AttachmentMetadata.java                # 메타데이터 DTO
    │   ├── client/
    │   │   ├── AttachmentServiceClient.java           # Feign 클라이언트
    │   │   └── AttachmentServiceClientFallback.java   # Fallback 구현
    │   ├── service/
    │   │   └── AttachmentClientService.java           # 검증/변환 서비스
    │   └── config/
    │       └── AttachmentClientAutoConfiguration.java # 자동 설정
    └── build.gradle
```

### 사용 방법

```gradle
// build.gradle에 의존성 추가만 하면 모든 기능 사용 가능
dependencies {
    implementation project(':libs:attachment-client-starter')
}
```

```java
// 기존과 동일한 패턴으로 사용
@Entity
public class NewsArticle {
    @ElementCollection
    @CollectionTable(name = "article_attachments", joinColumns = @JoinColumn(name = "article_id"))
    private List<AttachmentInfo> attachments = new ArrayList<>();
}

@Service
public class NewsService {
    private final AttachmentClientService attachmentClientService;
    
    public void createNews(CreateNewsRequest request) {
        List<AttachmentInfo> attachments = attachmentClientService.validateAndConvertAttachments(request.getAttachments());
        // ...
    }
}
```

## 결론

첨부파일 기능을 공통 라이브러리로 분리하면, 전체 시스템의 일관성과 개발 효율성을 크게 향상시킬 수 있을 것 같습니다.
attachment-service를 중심으로 한 중앙집중식 파일 관리 아키텍처와 완벽하게 연동되는 클라이언트 라이브러리가 될 것입니다.
