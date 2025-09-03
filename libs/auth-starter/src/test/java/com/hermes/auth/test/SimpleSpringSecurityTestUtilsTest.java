package com.hermes.auth.test;

import com.hermes.auth.principal.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringSecurityTestUtils의 기본 기능만 테스트하는 클래스
 */
class SimpleSpringSecurityTestUtilsTest {

    @AfterEach
    void tearDown() {
        SpringSecurityTestUtils.clearSecurityContext();
    }

    @Test
    void testSetAdminUser() {
        // When
        SpringSecurityTestUtils.setAdminUser(1L);
        
        // Then
        assertTrue(SpringSecurityTestUtils.isCurrentTestUserAdmin());
        assertEquals(1L, SpringSecurityTestUtils.getCurrentTestUserId());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testSetUserUser() {
        // When
        SpringSecurityTestUtils.setUserUser(2L);
        
        // Then
        assertFalse(SpringSecurityTestUtils.isCurrentTestUserAdmin());
        assertEquals(2L, SpringSecurityTestUtils.getCurrentTestUserId());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testSetUnauthenticated() {
        // Given
        SpringSecurityTestUtils.setAdminUser(1L);
        
        // When
        SpringSecurityTestUtils.setUnauthenticated();
        
        // Then
        assertNull(SpringSecurityTestUtils.getCurrentTestUserId());
        assertFalse(SpringSecurityTestUtils.isCurrentTestUserAdmin());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void testGetCurrentTestUserPrincipal() {
        // When
        SpringSecurityTestUtils.setAuthenticatedUser(100L, "ADMIN", "tenant-123");
        
        // Then
        UserPrincipal principal = SpringSecurityTestUtils.getCurrentTestUserPrincipal();
        assertNotNull(principal);
        assertEquals(100L, principal.getId());
        assertEquals("ADMIN", principal.getRoleString());
        assertEquals("tenant-123", principal.getTenantId());
    }
}