package com.hermes.communicationservice.websocket.config;

import com.hermes.auth.jwt.JwtAuthenticationConverter;
import com.hermes.auth.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * STOMP 메시지 레벨에서 인증을 처리하는 인터셉터
 * <p>
 * WebSocket 연결 과정: 1. HTTP 핸드셰이크 (Spring Security permitAll()로 통과) 2. WebSocket 연결 성립 3. STOMP
 * CONNECT 명령 (이 인터셉터에서 JWT 토큰 검증) 4. 이후 모든 STOMP 메시지들...
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JwtDecoder jwtDecoder;
  private final JwtAuthenticationConverter jwtAuthenticationConverter;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
        StompHeaderAccessor.class);

    // STOMP CONNECT 명령일 때만 인증 처리
    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      try {
        // STOMP 헤더에서 Authorization 토큰 추출
        List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");

        if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
          String authHeader = authorizationHeaders.get(0);

          // Bearer 토큰 형식 확인
          if (authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " 제거

            // JWT 토큰 검증
            Jwt jwt = jwtDecoder.decode(token);
            Authentication authentication = jwtAuthenticationConverter.convert(jwt);

            if (authentication != null
                && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
              // STOMP 세션에 사용자 정보 설정
              accessor.getSessionAttributes().put("userId", userPrincipal.getId());
              accessor.getSessionAttributes().put("role", userPrincipal.getRoleString());
              accessor.getSessionAttributes().put("user", userPrincipal);

              log.info("STOMP 연결 인증 성공 - userId: {}, role: {}",
                  userPrincipal.getId(), userPrincipal.getRoleString());

              return message;
            }
          }
        }

        // 토큰이 없거나 유효하지 않은 경우
        log.warn("STOMP 연결 실패 - 유효한 Authorization 헤더가 없습니다");
        return null; // null 반환으로 연결 거부

      } catch (Exception e) {
        log.error("STOMP 연결 인증 중 오류 발생: {}", e.getMessage());
        return null; // null 반환으로 연결 거부
      }
    }

    return message;
  }
}