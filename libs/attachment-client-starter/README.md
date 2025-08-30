# Attachment Client Starter

Hermes 마이크로서비스 시스템에서 첨부파일 기능을 위한 Spring Boot Starter 라이브러리입니다.

## 개요

이 라이브러리는 attachment-service와 연동하여 첨부파일 기능을 제공하는 공통 라이브러리입니다. Spring Boot Auto Configuration을 통해 간단한 의존성 추가만으로 첨부파일 관련 기능을 사용할 수 있습니다.

## 주요 기능

- **JPA 엔터티**: `AttachmentInfo` - 첨부파일 정보를 저장하는 임베디드 엔터티
- **DTO 클래스들**: 요청/응답/메타데이터 처리
- **Feign 클라이언트**: attachment-service와의 통신
- **비즈니스 로직**: 첨부파일 검증 및 변환 서비스
- **Circuit Breaker**: 장애 대응을 위한 Fallback 구현

## 사용 방법

### 1. 의존성 추가

```gradle
dependencies {
    implementation project(':libs:attachment-client-starter')
}
```

### 2. 엔터티에서 사용

```java
@Entity
public class Document {
    @ElementCollection
    @CollectionTable(name = "document_attachments", joinColumns = @JoinColumn(name = "document_id"))
    private List<AttachmentInfo> attachments = new ArrayList<>();
}
```

### 3. 서비스에서 사용

```java
@Service
public class DocumentService {
    private final AttachmentClientService attachmentClientService;
    
    public void createDocument(CreateDocumentRequest request) {
        // 첨부파일 검증 및 변환
        List<AttachmentInfo> attachments = attachmentClientService
            .validateAndConvertAttachments(request.getAttachments());
        
        // 문서 생성 로직...
    }
    
    public DocumentResponse getDocument(Long id) {
        Document document = documentRepository.findById(id);
        
        // 응답용 첨부파일 정보 변환
        List<AttachmentInfoResponse> attachments = attachmentClientService
            .convertToResponseList(document.getAttachments());
            
        // 응답 생성...
    }
}
```

## 구성 요소

### 엔터티

#### AttachmentInfo
```java
@Embeddable
public class AttachmentInfo {
    private String fileId;           // 파일 ID
    private String displayFileName;  // 표시할 파일명
    private Long fileSize;          // 파일 크기
    private String contentType;     // 콘텐츠 타입
}
```

### DTO 클래스들

#### AttachmentInfoRequest
첨부파일 요청 DTO
```java
public class AttachmentInfoRequest {
    private String fileId;
    private String displayFileName;
}
```

#### AttachmentInfoResponse
첨부파일 응답 DTO
```java
public class AttachmentInfoResponse {
    private String fileId;
    private String displayFileName;
    private Long fileSize;
    private String contentType;
    private String downloadUrl;
}
```

#### AttachmentMetadata
attachment-service로부터 받는 메타데이터
```java
public class AttachmentMetadata {
    private String fileId;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String filePath;
}
```

### 서비스

#### AttachmentClientService
주요 비즈니스 로직을 처리하는 서비스:

- `validateAndConvertAttachments()`: 첨부파일 목록 검증 및 변환
- `validateAndConvertAttachment()`: 단일 첨부파일 검증 및 변환
- `convertToResponseList()`: 엔터티를 응답 DTO로 변환
- `convertToResponse()`: 단일 엔터티를 응답 DTO로 변환

### Feign 클라이언트

#### AttachmentServiceClient
attachment-service와의 통신을 담당:

```java
@FeignClient(name = "attachment-service", fallback = AttachmentServiceClientFallback.class)
public interface AttachmentServiceClient {
    @GetMapping("/internal/attachments/{fileId}/metadata")
    AttachmentMetadata getFileMetadata(@PathVariable String fileId);
}
```

### 설정

#### 라이브러리 비활성화 (선택사항)
```yaml
hermes:
  attachment:
    enabled: false  # 기본값: true
```

## Auto Configuration

Spring Boot Auto Configuration을 통해 다음 컴포넌트들이 자동으로 구성됩니다:

- `AttachmentServiceClient`: Feign 클라이언트
- `AttachmentServiceClientFallback`: Circuit Breaker 패턴 구현
- `AttachmentClientService`: 비즈니스 로직 서비스

## 장애 대응

attachment-service가 응답하지 않을 경우 `AttachmentServiceClientFallback`이 작동하여 모의 데이터를 반환합니다.

```java
@Override
public AttachmentMetadata getFileMetadata(String fileId) {
    log.warn("attachment-service is not available, returning mock data for fileId: {}", fileId);
    
    AttachmentMetadata metadata = new AttachmentMetadata();
    metadata.setFileId(fileId);
    metadata.setOriginalFileName("mock-file.pdf");
    metadata.setFileSize(1024L);
    metadata.setContentType("application/pdf");
    metadata.setFilePath("/mock/path/" + fileId);
    
    return metadata;
}
```

## 보안

- XSS 방지를 위한 파일명 정리 기능 내장
- attachment-service에서 검증된 메타데이터만 사용

## 요구사항

- Java 17+
- Spring Boot 3.x
- Spring Cloud 2025.x
- attachment-service 연동 필요

## 마이그레이션 가이드

기존 서비스에서 이 라이브러리로 마이그레이션하는 경우:

1. 기존 첨부파일 관련 클래스들 제거
2. `implementation project(':libs:attachment-client-starter')` 의존성 추가
3. Import 문을 `com.hermes.attachment.*`로 변경
4. `AttachmentService` → `AttachmentClientService`로 변경