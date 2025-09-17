package com.hermes.multitenancy.interceptor;

import com.hermes.auth.principal.UserPrincipal;
import com.hermes.multitenancy.annotation.NonTenant;
import com.hermes.multitenancy.config.MultiTenancyProperties;
import com.hermes.multitenancy.context.TenantContext;
import com.hermes.multitenancy.exception.TenantAuthenticationException;
import com.hermes.multitenancy.exception.TenantNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 테넌트 컨텍스트 설정 인터셉터
 * @NonTenant 어노테이션 확인 및 JWT에서 테넌트 정보 추출
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true", matchIfMissing = true)
public class TenantContextInterceptor implements HandlerInterceptor {

    private final MultiTenancyProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) throws Exception {

        String requestPath = request.getRequestURI();

        try {
            // Controller 메소드인지 확인
            if (handler instanceof HandlerMethod handlerMethod) {

                // @NonTenant 어노테이션 확인 (메소드 > 클래스 순)
                NonTenant nonTenant = handlerMethod.getMethodAnnotation(NonTenant.class);
                if (nonTenant == null) {
                    nonTenant = handlerMethod.getBeanType().getAnnotation(NonTenant.class);
                }

                if (nonTenant != null) {
                    // @NonTenant가 있으면 테넌트 격리 적용 안함
                    log.debug("NonTenant endpoint accessed: {}", requestPath);
                    TenantContext.setNonTenant();
                    return true;
                }
            }

            // @NonTenant가 없으면 JWT에서 테넌트 정보 추출 (필수)
            String tenantId = extractTenantInfoFromSecurityContext(request);
            TenantContext.setTenantId(tenantId);

            if (properties.getSecurity().isEnableSecurityLogging()) {
                log.debug("Tenant context set: {} for path: {}", tenantId, requestPath);
            }

            return true;

        } catch (Exception e) {
            log.error("Failed to set tenant context for path: {}", requestPath, e);
            throw e;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler,
                               Exception ex) {
        // 요청 완료 후 컨텍스트 정리
        TenantContext.clear();
        if (properties.getSecurity().isEnableSecurityLogging()) {
            log.debug("Tenant context cleared for path: {}", request.getRequestURI());
        }
    }

    /**
     * Spring Security의 SecurityContext에서 테넌트 정보 추출
     */
    private String extractTenantInfoFromSecurityContext(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않은 요청 처리
        if (auth == null || !auth.isAuthenticated()) {
            return handleMissingAuthentication(request);
        }

        try {
            if (auth instanceof JwtAuthenticationToken jwtToken) {
                Object details = jwtToken.getDetails();
                if (details instanceof UserPrincipal userPrincipal) {
                    String tenantId = userPrincipal.getTenantId();

                    if (StringUtils.hasText(tenantId)) {
                        if (properties.getSecurity().isEnableSecurityLogging()) {
                            log.debug("Tenant info extracted from SecurityContext: {}", tenantId);
                        }
                        return tenantId;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract tenant info from SecurityContext: {}", e.getMessage());
            return handleTenantExtractionFailure(request, e);
        }

        // 테넌트 정보가 없는 경우
        return handleMissingTenantInfo(request);
    }

    /**
     * 인증 정보가 없는 경우 처리
     */
    private String handleMissingAuthentication(HttpServletRequest request) {
        String message = String.format("Authentication required for path: %s", request.getRequestURI());

        switch (properties.getSecurity().getStrategy()) {
            case FAIL_FAST:
                log.warn(message);
                throw new TenantAuthenticationException(message);
            case LOG_AND_ALLOW:
            case ALLOW_DEFAULT:
                log.warn("{} - Cannot proceed without tenant information", message);
                throw new TenantAuthenticationException(message);
            default:
                throw new TenantAuthenticationException(message);
        }
    }

    /**
     * 테넌트 정보 추출 실패 시 처리
     */
    private String handleTenantExtractionFailure(HttpServletRequest request, Exception e) {
        String message = String.format("Failed to extract tenant info for path: %s", request.getRequestURI());

        switch (properties.getSecurity().getStrategy()) {
            case FAIL_FAST:
                throw new TenantNotFoundException(message, e);
            case LOG_AND_ALLOW:
            case ALLOW_DEFAULT:
                log.warn("{} - Cannot proceed without tenant information", message);
                throw new TenantNotFoundException(message, e);
            default:
                throw new TenantNotFoundException(message, e);
        }
    }

    /**
     * 테넌트 정보가 없는 경우 처리
     */
    private String handleMissingTenantInfo(HttpServletRequest request) {
        String message = String.format("Tenant information not found for path: %s", request.getRequestURI());

        switch (properties.getSecurity().getStrategy()) {
            case FAIL_FAST:
                log.warn(message);
                throw new TenantNotFoundException(message);
            case LOG_AND_ALLOW:
            case ALLOW_DEFAULT:
                log.warn("{} - Cannot proceed without tenant information", message);
                throw new TenantNotFoundException(message);
            default:
                throw new TenantNotFoundException(message);
        }
    }
}