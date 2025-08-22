package com.hermes.multitenancy.filter;

import com.hermes.jwt.JwtTokenProvider;
import com.hermes.jwt.context.UserInfo;
import com.hermes.multitenancy.context.TenantContext;
import com.hermes.multitenancy.dto.TenantInfo;
import com.hermes.multitenancy.util.TenantUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 테넌트 컨텍스트 설정 필터
 * HTTP 요청에서 JWT 토큰을 추출하여 테넌트 정보를 TenantContext에 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true", matchIfMissing = true)
public class TenantContextFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 테넌트 정보 추출 및 설정
            TenantInfo tenantInfo = extractTenantInfo(request);
            
            if (tenantInfo != null) {
                TenantContext.setTenant(tenantInfo);
                log.debug("Tenant context set for request: {}", tenantInfo.getTenantId());
            } else {
                // 기본 테넌트 설정
                TenantContext.setTenant(getDefaultTenantInfo());
                log.debug("Using default tenant for request");
            }
            
            // 다음 필터로 진행
            filterChain.doFilter(request, response);
            
        } finally {
            // 요청 완료 후 컨텍스트 정리
            TenantContext.clear();
            log.debug("Tenant context cleared");
        }
    }

    /**
     * HTTP 요청에서 테넌트 정보 추출 (JWT에서만)
     */
    private TenantInfo extractTenantInfo(HttpServletRequest request) {
        // JWT 토큰에서만 테넌트 정보 추출
        String token = extractTokenFromRequest(request);
        if (token != null) {
            try {
                UserInfo userInfo = jwtTokenProvider.getUserInfoFromToken(token);
                String tenantId = userInfo.getTenantId();
                
                if (StringUtils.hasText(tenantId)) {
                    String schemaName = TenantUtils.generateSchemaName(tenantId);
                    log.debug("Tenant info extracted from JWT: {}", tenantId);
                    return new TenantInfo(tenantId, schemaName);
                }
            } catch (Exception e) {
                log.debug("Failed to extract tenant info from JWT token: {}", e.getMessage());
            }
        }
        
        return null;
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }



    /**
     * 기본 테넌트 정보 반환
     */
    private TenantInfo getDefaultTenantInfo() {
        return new TenantInfo(
            TenantContext.DEFAULT_TENANT_ID,
            TenantContext.DEFAULT_SCHEMA_NAME
        );
    }

    /**
     * 특정 경로는 필터링 제외 (헬스체크, 정적 리소스 등)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        return path.startsWith("/actuator/") ||
               path.startsWith("/health") ||
               path.startsWith("/static/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/");
    }
}
