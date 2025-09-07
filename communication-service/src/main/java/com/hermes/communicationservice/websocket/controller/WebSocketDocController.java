package com.hermes.communicationservice.websocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.Builder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * WebSocket 연결 정보를 문서화하기 위한 컨트롤러
 * 실제 API 엔드포인트가 아닌 문서화 목적
 */
@RestController
@RequestMapping("/api/websocket-docs")
@Tag(name = "WebSocket 정보", description = "WebSocket 실시간 알림 연결 가이드")
public class WebSocketDocController {
    
    @Operation(
        summary = "WebSocket 연결 정보",
        description = """
            ## WebSocket 연결 방법
            
            ### 1. 연결 엔드포인트
            - **SockJS**: `/ws` (폴백 지원)
            - **순수 WebSocket**: `/ws-pure`
            
            ### 2. 구독 채널
            - **개인 알림**: `/user/queue/notifications` (결재 관련)
            - **전체 공지**: `/topic/announcements` (공지사항)
            
            ### 3. 연결 예시
            ```javascript
            // SockJS 사용
            const socket = new SockJS('http://gateway-url/ws');
            const stompClient = Stomp.over(socket);
            
            // 연결
            stompClient.connect(
                { Authorization: 'Bearer YOUR_JWT_TOKEN' },
                (frame) => {
                    console.log('Connected: ' + frame);
                    
                    // 개인 알림 구독
                    stompClient.subscribe('/user/queue/notifications', (message) => {
                        const notification = JSON.parse(message.body);
                        console.log('개인 알림:', notification);
                    });
                    
                    // 공지사항 구독
                    stompClient.subscribe('/topic/announcements', (message) => {
                        const notification = JSON.parse(message.body);
                        console.log('공지사항:', notification);
                    });
                },
                (error) => {
                    console.error('연결 실패:', error);
                }
            );
            ```
            
            ### 4. 알림 메시지 형식
            
            **개인 알림 (결재 관련)**:
            ```json
            {
                "notificationId": 123,
                "userId": 456,
                "type": "APPROVAL_REQUEST",
                "content": "새로운 결재 요청이 있습니다",
                "referenceId": 789,
                "createdAt": "2025-09-07T03:29:23.837203300Z"
            }
            ```
            
            **공지사항 알림**:
            ```json
            {
                "notificationId": 124,
                "userId": null,
                "type": "ANNOUNCEMENT",
                "content": "[공지사항] 새로운 공지사항이 등록되었습니다",
                "referenceId": 39,
                "createdAt": "2025-09-07T03:29:23.837203300Z"
            }
            ```
            
            ### 5. 알림 타입
            - `ANNOUNCEMENT`: 공지사항 (전체 브로드캐스트)
            - `APPROVAL_REQUEST`: 결재 요청 (개인 알림)
            - `APPROVAL_APPROVED`: 결재 승인 (개인 알림)
            - `APPROVAL_REJECTED`: 결재 반려 (개인 알림)
            - `APPROVAL_REFERENCE`: 결재 참조 (개인 알림)
            
            ### 6. 채널별 구독 방법
            - **공지사항만 받기**: `/topic/announcements` 구독
            - **개인 알림만 받기**: `/user/queue/notifications` 구독  
            - **모든 알림 받기**: 두 채널 모두 구독
            """
    )
    @GetMapping("/info")
    public WebSocketInfo getWebSocketInfo() {
        return WebSocketInfo.builder()
            .endpoints(List.of(
                EndpointInfo.builder()
                    .url("/ws")
                    .type("SockJS")
                    .description("SockJS 연결 (폴백 지원)")
                    .build(),
                EndpointInfo.builder()
                    .url("/ws-pure")
                    .type("Pure WebSocket")
                    .description("순수 WebSocket 연결")
                    .build()
            ))
            .channels(List.of(
                ChannelInfo.builder()
                    .channel("/user/queue/notifications")
                    .type("개인 알림")
                    .description("특정 사용자에게 전송되는 개인 알림")
                    .build(),
                ChannelInfo.builder()
                    .channel("/topic/announcements")
                    .type("공지사항 브로드캐스트")
                    .description("전체 사용자에게 전송되는 공지사항 알림")
                    .build()
            ))
            .build();
    }
    
    @Data
    @Builder
    public static class WebSocketInfo {
        private List<EndpointInfo> endpoints;
        private List<ChannelInfo> channels;
    }
    
    @Data
    @Builder
    public static class EndpointInfo {
        private String url;
        private String type;
        private String description;
    }
    
    @Data
    @Builder
    public static class ChannelInfo {
        private String channel;
        private String type;
        private String description;
    }
}