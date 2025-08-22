package com.hermes.multitenancy.jwt;

import com.hermes.jwt.JwtTokenProvider;
import com.hermes.jwt.context.UserInfo;
import com.hermes.multitenancy.dto.TenantInfo;
import com.hermes.multitenancy.util.TenantUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JWT에서 테넌트 정보를 추출하는 유틸리티
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantJwtExtractor {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * JWT 토큰에서 테넌트 정보 추출 (1번 파싱으로 최적화)
     */
    public TenantInfo extractTenantInfo(String token) {
        try {
            // JWT 토큰에서 사용자 정보 추출 (유효성 검증 포함) - 1번만 파싱!
            UserInfo userInfo = jwtTokenProvider.getUserInfoFromToken(token);
            
            // 1순위: JWT에 tenantId가 직접 포함된 경우
            String tenantId = userInfo.getTenantId();
            if (tenantId != null && !tenantId.isEmpty()) {
                String schemaName = TenantUtils.generateSchemaName(tenantId);
                log.debug("Found tenant ID in JWT: {}", tenantId);
                return new TenantInfo(tenantId, tenantId, schemaName, "ACTIVE");
            }
            
            // 2순위: 이메일 도메인에서 테넌트 추론
            String email = userInfo.getEmail();
            if (email != null && email.contains("@")) {
                String inferredTenantId = convertDomainToTenantId(email.substring(email.indexOf("@") + 1));
                if (inferredTenantId != null) {
                    String schemaName = TenantUtils.generateSchemaName(inferredTenantId);
                    log.debug("Inferred tenant from email domain: {} -> {}", email, inferredTenantId);
                    return new TenantInfo(inferredTenantId, inferredTenantId, schemaName, "ACTIVE");
                }
            }
            
            log.warn("No tenant information found in JWT token");
            return null;
            
        } catch (Exception e) {
            log.error("Failed to extract tenant info from JWT token: {}", e.getMessage());
            return null;
        }
    }


    /**
     * 이메일 도메인을 테넌트 ID로 변환
     */
    private String convertDomainToTenantId(String domain) {
        if (domain == null || domain.isEmpty()) {
            return null;
        }
        
        // 도메인을 테넌트 ID로 변환하는 로직
        // 예: company.com -> company
        String tenantId = domain.replaceAll("\\.[^.]+$", "");
        tenantId = tenantId.replaceAll("[^a-zA-Z0-9]", "");
        
        // 유효한 테넌트 ID인지 확인
        if (tenantId.isEmpty() || tenantId.length() > 50) {
            return null;
        }
        
        return tenantId.toLowerCase();
    }


    /**
     * JWT 토큰의 유효성 확인
     */
    public boolean isValidToken(String token) {
        try {
            jwtTokenProvider.getUserInfoFromToken(token);
            return true;
        } catch (Exception e) {
            log.debug("Invalid JWT token", e);
            return false;
        }
    }
}