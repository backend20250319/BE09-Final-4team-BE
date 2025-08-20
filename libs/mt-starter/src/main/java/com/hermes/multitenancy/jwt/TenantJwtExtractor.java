package com.hermes.multitenancy.jwt;

import com.hermes.jwt.JwtTokenProvider;
import com.hermes.multitenancy.dto.TenantInfo;
import com.hermes.multitenancy.service.TenantService;
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
    private final TenantService tenantService;

    /**
     * JWT 토큰에서 테넌트 정보 추출
     */
    public TenantInfo extractTenantInfo(String token) {
        try {
            // JWT 토큰 유효성 검증
            if (!jwtTokenProvider.isValidToken(token)) {
                log.warn("Invalid JWT token provided");
                return null;
            }
            
            // 테넌트 ID 추출 시도
            String tenantId = extractTenantId(token);
            
            if (tenantId != null && !tenantId.isEmpty()) {
                // 테넌트 서비스에서 테넌트 정보 조회
                TenantInfo tenantInfo = tenantService.getTenantInfo(tenantId);
                if (tenantInfo != null) {
                    return tenantInfo;
                }
            }
            
            // 테넌트 ID가 없거나 찾을 수 없으면 사용자 이메일로 테넌트 추론
            String email = jwtTokenProvider.getEmailFromToken(token);
            if (email != null) {
                return tenantService.getTenantInfoByUserEmail(email);
            }
            
            log.warn("No tenant information found in JWT token");
            return null;
            
        } catch (Exception e) {
            log.error("Failed to extract tenant info from JWT token", e);
            return null;
        }
    }

    /**
     * JWT 토큰에서 테넌트 ID 추출
     */
    private String extractTenantId(String token) {
        try {
            // JWT 클레임에서 "tenantId" 필드 확인
            String tenantId = jwtTokenProvider.getClaimFromToken(token, "tenantId");
            if (tenantId != null && !tenantId.isEmpty()) {
                return tenantId;
            }

            // 클레임에서 "tenant" 필드 확인 (대안)
            tenantId = jwtTokenProvider.getClaimFromToken(token, "tenant");
            if (tenantId != null && !tenantId.isEmpty()) {
                return tenantId;
            }

            // 클레임에서 "org" 필드 확인 (대안)
            tenantId = jwtTokenProvider.getClaimFromToken(token, "org");
            if (tenantId != null && !tenantId.isEmpty()) {
                return tenantId;
            }

            // 이메일 도메인에서 테넌트 추론
            String email = jwtTokenProvider.getEmailFromToken(token);
            if (email != null && email.contains("@")) {
                String domain = email.substring(email.indexOf("@") + 1);
                return convertDomainToTenantId(domain);
            }

            return null;
            
        } catch (Exception e) {
            log.debug("Failed to extract tenant ID from JWT token", e);
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
            return jwtTokenProvider.isValidToken(token);
        } catch (Exception e) {
            log.debug("Invalid JWT token", e);
            return false;
        }
    }
}