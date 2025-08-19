package com.hermes.jwt.filter;

import com.hermes.jwt.JwtTokenProvider;
import com.hermes.jwt.JwtPayload;
import com.hermes.jwt.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${jwt.filter.whitelist:}")
    private List<String> whitelistPaths;

    @Value("${security.jwt.header:Authorization}")
    private String authHeader;

    @Value("${security.jwt.prefix:Bearer}")
    private String tokenPrefix;

    @PostConstruct
    public void init() {
        if (whitelistPaths == null || whitelistPaths.isEmpty()) {
            whitelistPaths = Arrays.asList(
           "/user/login",
                "/auth/login",
                "/token/generate", 
                "/token/refresh",
                "/token/validate",
                "/token/parse-email",
                "/actuator/health",
                "/actuator/info",
                "/error",
                "/favicon.ico"
            );
            log.info("Using default whitelist paths: {}", whitelistPaths);
        } else {
            log.info("Loaded whitelist paths from configuration: {}", whitelistPaths);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("JWT Filter - Request: {} {}", method, requestURI);

        if (isWhitelistedPath(requestURI)) {
            log.debug("Whitelisted path: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractToken(request);
            if (token != null && jwtTokenProvider.isValidToken(token)) {

                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.warn("Blacklisted token used for request: {}", requestURI);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                
                JwtPayload payload = jwtTokenProvider.getPayloadFromToken(token);
                setUserContext(request, payload);
                log.debug("JWT validation successful for user: {}", payload.getEmail());
            } else {
                log.warn("Invalid or missing JWT token for request: {}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            log.error("JWT validation failed for request: {}", requestURI, e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelistedPath(String requestURI) {
        return whitelistPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeaderValue = request.getHeader(authHeader);
        if (authHeaderValue != null && authHeaderValue.startsWith(tokenPrefix + " ")) {
            return authHeaderValue.substring(tokenPrefix.length() + 1);
        }
        return null;
    }

    private void setUserContext(HttpServletRequest request, JwtPayload payload) {
        request.setAttribute("userId", payload.getUserId());
        request.setAttribute("email", payload.getEmail());
        request.setAttribute("role", payload.getRole());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI.startsWith("/actuator") || 
               requestURI.startsWith("/error") ||
               requestURI.startsWith("/favicon.ico");
    }
}
