package com.hermes.jwt.filter;

import com.hermes.jwt.context.AuthContext;
import com.hermes.jwt.context.UserInfo;
import com.hermes.jwt.context.Role;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * HTTP 헤더에서 사용자 정보를 추출하여 AuthContext에 설정하는 필터
 * Gateway에서 JWT 검증 후 주입한 헤더를 처리합니다.
 */
@Slf4j
@Component
@Order(1) // 다른 필터들보다 먼저 실행
public class AuthContextFilter implements Filter {
    
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            processAuthHeaders(httpRequest);
        }
        
        try {
            chain.doFilter(request, response);
        } finally {
            // 요청 처리 완료 후 ThreadLocal 정리 (메모리 누수 방지)
            AuthContext.clear();
        }
    }
    
    /**
     * HTTP 헤더에서 사용자 정보를 추출하여 AuthContext에 설정
     */
    private void processAuthHeaders(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        String email = request.getHeader(HEADER_USER_EMAIL);
        String role = request.getHeader(HEADER_USER_ROLE);
        String tenantId = request.getHeader(HEADER_TENANT_ID);
        
        log.debug("Processing auth headers: userId={}, email={}, role={}, tenantId={}", 
                 userId, email, role, tenantId);
        
        // 사용자 ID가 있는 경우에만 AuthContext 설정
        if (StringUtils.hasText(userId)) {
            try {
                Role userRole = Role.fromString(role, Role.USER); // 기본값은 USER
                
                UserInfo userInfo = UserInfo.builder()
                        .userId(Long.valueOf(userId))
                        .email(email)
                        .role(userRole)
                        .tenantId(tenantId)
                        .build();
                
                AuthContext.setCurrentUser(userInfo);
                
                log.debug("AuthContext set successfully: userId={}, email={}, role={}", 
                         userInfo.getUserId(), userInfo.getEmail(), userInfo.getRoleString());
                
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID format in header: {}", userId);
                // 유효하지 않은 사용자 ID인 경우 AuthContext를 설정하지 않음
            }
        } else {
            log.debug("No user ID header found, skipping AuthContext setup");
        }
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