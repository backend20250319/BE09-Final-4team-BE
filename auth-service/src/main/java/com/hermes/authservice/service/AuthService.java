package com.hermes.authservice.service;

import com.hermes.authservice.dto.LoginRequestDto;
import com.hermes.authservice.dto.UserAuthInfo;
import com.hermes.authservice.entity.RefreshToken;
import com.hermes.authservice.repository.RefreshTokenRepository;
import com.hermes.jwt.JwtTokenProvider;
import com.hermes.jwt.dto.TokenResponse;
import com.hermes.jwt.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserIntegrationService userIntegrationService;
    private final AuthenticationService authenticationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public TokenResponse login(LoginRequestDto loginDto) {
        log.info(" [Auth Service] 로그인 처리 시작 - email: {}", loginDto.getEmail());

        try {
            // 사용자 정보 조회 (user-service에서)
            UserAuthInfo user = userIntegrationService.getUserByEmail(loginDto.getEmail());

            if (user == null) {
                log.error(" [Auth Service] 사용자 정보가 null입니다 - email: {}", loginDto.getEmail());
                throw new RuntimeException("해당 이메일로 등록된 사용자가 없습니다.");
            }

            // 비밀번호 검증
            if (!authenticationService.validatePassword(loginDto.getPassword(), user.getPassword())) {
                log.error(" [Auth Service] 비밀번호 검증 실패 - email: {}", loginDto.getEmail());
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }

            // 토큰 생성
            String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getId(),
                    user.getIsAdmin() ? "ADMIN" : "USER");
            String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()), user.getEmail());

            // 기존 RefreshToken이 있으면 삭제
            refreshTokenRepository.findByUserId(user.getId()).ifPresent(existingToken -> {
                refreshTokenRepository.delete(existingToken);
                log.info(" [Auth Service] 기존 RefreshToken 삭제 - userId: {}", user.getId());
            });

            // 새로운 RefreshToken 저장
            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .userId(user.getId())
                            .token(refreshToken)
                            .expiration(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000))
                            .build()
            );

            // 마지막 로그인 시간 업데이트
            userIntegrationService.updateLastLogin(user.getId());

            log.info(" [Auth Service] 로그인 성공 - email: {}, userId: {}", loginDto.getEmail(), user.getId());
            return new TokenResponse(accessToken, refreshToken);

        } catch (Exception e) {
            log.error(" [Auth Service] 로그인 처리 중 오류 발생 - email: {}, error: {}", loginDto.getEmail(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void logout(Long userId, String accessToken) {
        log.info(" [Auth Service] 로그아웃 처리 시작 - userId: {}", userId);

        try {
            // RefreshToken을 DB에서 삭제
            refreshTokenRepository.findByUserId(userId).ifPresent(token -> {
                refreshTokenRepository.delete(token);
                log.info("[Auth Service] RefreshToken 삭제 완료 - userId: {}", userId);
            });

            // TokenBlacklistService를 통해 모든 토큰 완전 삭제
            tokenBlacklistService.logoutUser(userId, accessToken, null);
            log.info(" [Auth Service] 모든 토큰 완전 삭제 완료 - userId: {}", userId);

        } catch (Exception e) {
            log.error("[Auth Service] 로그아웃 처리 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    // 만료된 RefreshToken 자동 정리 (매 1시간마다 실행)
    @Scheduled(fixedRate = 3600000) // 1시간 = 3,600,000ms
    @Transactional
    public void cleanupExpiredTokens() {
        log.info(" [Auth Service] 만료된 RefreshToken 정리 시작");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<RefreshToken> expiredTokens = refreshTokenRepository.findByExpirationBefore(now);
            
            if (!expiredTokens.isEmpty()) {
                refreshTokenRepository.deleteAll(expiredTokens);
                log.info(" [Auth Service] 만료된 RefreshToken {}개 삭제 완료", expiredTokens.size());
                
                // 만료된 토큰들의 사용자 ID 로깅
                expiredTokens.forEach(token -> 
                    log.info(" [Auth Service] 만료된 토큰 삭제 - userId: {}", token.getUserId())
                );
            } else {
                log.info(" [Auth Service] 삭제할 만료된 RefreshToken이 없습니다.");
            }
            
        } catch (Exception e) {
            log.error(" [Auth Service] 만료된 RefreshToken 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}