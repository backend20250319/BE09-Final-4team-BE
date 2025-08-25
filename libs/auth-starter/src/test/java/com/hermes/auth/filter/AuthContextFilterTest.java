package com.hermes.auth.filter;

import com.hermes.auth.JwtTokenProvider;
import com.hermes.auth.context.AuthContext;
import com.hermes.auth.context.Role;
import com.hermes.auth.context.UserInfo;
import com.hermes.auth.test.AuthTestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthContextFilterTest {
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private AuthContextFilter authContextFilter;
    
    @BeforeEach
    void setUp() {
        AuthTestUtils.clearAuthContext();
    }
    
    @AfterEach
    void tearDown() {
        AuthTestUtils.clearAuthContext();
    }
    
    @Test
    void testValidJwtToken() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;
        
        UserInfo expectedUserInfo = UserInfo.builder()
                .userId(100L)
                .email("test@example.com")
                .role(Role.USER)
                .tenantId("test-tenant")
                .build();
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtTokenProvider.getUserInfoFromToken(validToken)).thenReturn(expectedUserInfo);
        
        // AuthContext 검증을 위해 filterChain에서 확인
        doAnswer(invocation -> {
            // filterChain.doFilter가 호출되는 시점에 AuthContext 검증
            assertEquals(100L, AuthContext.getCurrentUserId());
            assertEquals("test@example.com", AuthContext.getCurrentUser().getEmail());
            assertEquals(Role.USER, AuthContext.getCurrentUser().getRole());
            assertEquals("test-tenant", AuthContext.getCurrentUser().getTenantId());
            return null;
        }).when(filterChain).doFilter(request, response);
        
        // When
        authContextFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).getUserInfoFromToken(validToken);
    }
    
    @Test
    void testInvalidJwtToken() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.jwt.token";
        String authHeader = "Bearer " + invalidToken;
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtTokenProvider.getUserInfoFromToken(invalidToken)).thenThrow(new RuntimeException("Invalid token"));
        
        // When
        authContextFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).getUserInfoFromToken(invalidToken);
        
        // AuthContext가 설정되지 않았는지 확인
        assertNull(AuthContext.getCurrentUserId());
    }
    
    @Test
    void testNoAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // When
        authContextFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).getUserInfoFromToken(any());
        
        // AuthContext가 설정되지 않았는지 확인
        assertNull(AuthContext.getCurrentUserId());
    }
    
    @Test
    void testMalformedAuthorizationHeader() throws ServletException, IOException {
        // Given
        String malformedHeader = "InvalidHeader without Bearer";
        when(request.getHeader("Authorization")).thenReturn(malformedHeader);
        
        // When
        authContextFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).getUserInfoFromToken(any());
        
        // AuthContext가 설정되지 않았는지 확인
        assertNull(AuthContext.getCurrentUserId());
    }
    
    @Test
    void testAuthContextClearAfterRequest() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;
        
        UserInfo userInfo = UserInfo.builder()
                .userId(100L)
                .email("test@example.com")
                .role(Role.USER)
                .tenantId("test-tenant")
                .build();
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtTokenProvider.getUserInfoFromToken(validToken)).thenReturn(userInfo);
        
        // When
        authContextFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        
        // 필터 처리 후 AuthContext가 정리되었는지 확인 (finally 블록에서 clear됨)
        assertNull(AuthContext.getCurrentUserId());
    }
}