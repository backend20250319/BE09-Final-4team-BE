package com.hermes.multitenancy.filter;

import com.hermes.auth.context.AuthContext;
import com.hermes.auth.filter.AuthContextFilter;
import com.hermes.multitenancy.context.TenantContext;
import com.hermes.multitenancy.dto.TenantInfo;
import com.hermes.multitenancy.util.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * AuthContext에서 테넌트 정보를 가져와 TenantContext에 설정
 * AuthContextFilter 다음에 실행됨
 */
@Slf4j
@Component
@Order(AuthContextFilter.ORDER + 1) // AuthContextFilter 다음에 실행
@ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true", matchIfMissing = true)
public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // AuthContext에서 테넌트 정보 추출 및 설정
            TenantInfo tenantInfo = extractTenantInfoFromAuthContext();
            
            TenantContext.setTenant(tenantInfo);
            log.debug("Tenant context set: {}", tenantInfo.getTenantId());
            
            // 다음 필터로 진행
            filterChain.doFilter(request, response);
            
        } finally {
            // 요청 완료 후 컨텍스트 정리
            TenantContext.clear();
            log.debug("Tenant context cleared");
        }
    }

    /**
     * AuthContext에서 테넌트 정보 추출
     */
    private TenantInfo extractTenantInfoFromAuthContext() {
        try {
            // AuthContext에서 테넌트 ID 가져오기
            String tenantId = AuthContext.getCurrentTenantId();
            
            if (StringUtils.hasText(tenantId)) {
                String schemaName = TenantUtils.generateSchemaName(tenantId);
                log.debug("Tenant info extracted from AuthContext: {}", tenantId);
                return new TenantInfo(tenantId, schemaName);
            }
        } catch (Exception e) {
            log.debug("Failed to extract tenant info from AuthContext: {}", e.getMessage());
        }
        
        // 기본 테넌트 반환
        log.debug("Using default tenant");
        return getDefaultTenantInfo();
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
