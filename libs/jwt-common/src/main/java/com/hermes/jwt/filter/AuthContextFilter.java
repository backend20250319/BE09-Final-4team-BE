package com.hermes.jwt.filter;

import com.hermes.jwt.JwtTokenProvider;
import com.hermes.jwt.context.AuthContext;
import com.hermes.jwt.context.UserInfo;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * JWT 토큰에서 직접 사용자 정보를 추출하여 AuthContext에 설정하는 필터
 * 보안을 위해 HTTP 헤더가 아닌 JWT 토큰에서 직접 추출합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(AuthContextFilter.ORDER) // 다른 필터들보다 먼저 실행
public class AuthContextFilter implements Filter {
    
    /**
     * AuthContextFilter의 실행 순서
     * 다른 필터에서 이 값을 참조하여 순서를 보장할 수 있습니다.
     */
    public static final int ORDER = 1;
    
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            processJwtToken(httpRequest);
        }
        
        try {
            chain.doFilter(request, response);
        } finally {
            // 요청 처리 완료 후 ThreadLocal 정리 (메모리 누수 방지)
            AuthContext.clear();
        }
    }
    
    /**
     * JWT 토큰에서 사용자 정보를 추출하여 AuthContext에 설정
     */
    private void processJwtToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            log.debug("No JWT token found, skipping AuthContext setup");
            return;
        }
        
        try {
            // JWT 토큰에서 사용자 정보 추출 (유효성 검증 포함)
            UserInfo userInfo = jwtTokenProvider.getUserInfoFromToken(token);
            
            if (userInfo.getUserId() != null) {
                AuthContext.setCurrentUser(userInfo);
                
                log.debug("AuthContext set from JWT: userId={}, email={}, role={}, tenantId={}", 
                         userInfo.getUserId(), userInfo.getEmail(), userInfo.getRoleString(), userInfo.getTenantId());
            } else {
                log.debug("Invalid user ID from JWT token");
            }
            
        } catch (Exception e) {
            log.debug("Failed to process JWT token: {}", e.getMessage());
            // JWT 처리 실패시 AuthContext를 설정하지 않음 (정상 동작)
        }
    }
    
    /**
     * HTTP 요청에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("AuthContextFilter initialized");
    }
    
    @Override
    public void destroy() {
        log.info("AuthContextFilter destroyed");
    }
}