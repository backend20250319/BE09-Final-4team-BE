package com.hermes.communicationservice.websocket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final WebSocketAuthInterceptor webSocketAuthInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    log.info("WebSocket 메시지 브로커 설정 시작");

    // 클라이언트가 구독할 수 있는 경로 prefix
    // /topic: 1:N 브로드캐스트 (전체 알림)
    // /queue: 1:1 메시지 (개인 알림)
    config.enableSimpleBroker("/topic", "/queue");

    // 클라이언트에서 서버로 메시지 전송시 사용할 prefix
    config.setApplicationDestinationPrefixes("/app");

    // 특정 사용자에게 메시지 전송시 사용할 prefix
    config.setUserDestinationPrefix("/user");

    log.info("WebSocket 메시지 브로커 설정 완료");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    log.info("STOMP 엔드포인트 등록 시작");

    // WebSocket 연결 엔드포인트 (SockJS 지원)
    // SockJS: 일부 브라우저나 방화벽에서 WebSocket을 지원하지 않을 때 HTTP 폴링 등으로 대체해주는 라이브러리
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("http://localhost:3000")
        .withSockJS();  // SockJS 지원 활성화

    // 순수 WebSocket 엔드포인트 (SockJS 없이 순수한 WebSocket만 사용)
    // 모던 브라우저에서 WebSocket을 직접 지원할 때 사용
    registry.addEndpoint("/ws-pure")
        .setAllowedOriginPatterns("http://localhost:3000");

    log.info("STOMP 엔드포인트 등록 완료: /ws, /ws-pure");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    // WebSocket 인증 인터셉터 등록
    registration.interceptors(webSocketAuthInterceptor);
    log.info("WebSocket 인증 인터셉터 등록 완료");
  }
}