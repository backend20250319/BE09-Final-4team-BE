package com.hermes.auth.test;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JWT 기반 테스트 사용자 인증을 위한 커스텀 어노테이션
 * 
 * 사용 예시:
 * @WithMockJwtUser(userId = 1L, role = "ADMIN")
 * @Test
 * void testAdminFunction() {
 *     // 테스트 코드
 * }
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtUserSecurityContextFactory.class)
public @interface WithMockJwtUser {
    
    /**
     * 사용자 ID
     */
    long userId() default 1L;
    
    /**
     * 사용자 이메일
     */
    String email() default "test@example.com";
    
    /**
     * 사용자 역할 (ADMIN, USER)
     */
    String role() default "USER";
    
    /**
     * 테넌트 ID
     */
    String tenantId() default "test-tenant";
}