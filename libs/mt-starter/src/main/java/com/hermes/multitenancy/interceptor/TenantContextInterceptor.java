package com.hermes.multitenancy.interceptor;

import com.hermes.auth.principal.UserPrincipal;
import com.hermes.multitenancy.config.MultiTenancyProperties;
import com.hermes.multitenancy.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 테넌트 컨텍스트 설정 인터셉터
 * JWT에서 테넌트 정보 추출
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true", matchIfMissing = true)
public class TenantContextInterceptor implements HandlerInterceptor {

    private final MultiTenancyProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String requestPath = request.getRequestURI();

        try {
            // JWT에서 테넌트 정보 추출 시도
            String tenantId = extractTenantInfoFromSecurityContext(request);
            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
                log.debug("Tenant context set: {} for path: {}", tenantId, requestPath);
            } else {
                log.debug("No tenant context set for path: {}", requestPath);
            }

            return true;

        } catch (Exception e) {
            log.debug("Failed to set tenant context for path: {}, continuing without tenant context", requestPath);
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 요청 완료 후 컨텍스트 정리
        TenantContext.clear();
        log.debug("Tenant context cleared for path: {}", request.getRequestURI());
    }

    /**
     * Spring Security의 SecurityContext에서 테넌트 정보 추출
     */
    private String extractTenantInfoFromSecurityContext(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않은 요청 처리
        if (auth == null || !auth.isAuthenticated()) {
            log.debug("No authentication found for path: {}", request.getRequestURI());
            return null;
        }

        try {
            if (auth instanceof JwtAuthenticationToken jwtToken) {
                Object details = jwtToken.getDetails();
                if (details instanceof UserPrincipal userPrincipal) {
                    String tenantId = userPrincipal.getTenantId();

                    if (StringUtils.hasText(tenantId)) {
                        log.debug("Tenant info extracted from SecurityContext: {}", tenantId);
                        return tenantId;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract tenant info from SecurityContext: {}", e.getMessage());
            return null;
        }

        // 테넌트 정보가 없는 경우
        log.debug("No tenant info found in SecurityContext for path: {}", request.getRequestURI());
        return null;
    }

}