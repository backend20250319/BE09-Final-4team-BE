package com.hermes.userservice.service;

import com.hermes.jwt.JwtTokenProvider;
import com.hermes.userservice.service.TokenBlacklistService;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.InvalidCredentialsException;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.jwt.dto.TokenResponse;
import com.hermes.userservice.jwt.entity.RefreshToken;
import com.hermes.userservice.jwt.repository.RefreshTokenRepository;
import com.hermes.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    public TokenResponse login(LoginRequestDto loginDto) {
        // 사용자 조회
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        // 비밀번호 검증 (BCrypt 암호화된 비밀번호와 비교)
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            // 기존 평문 비밀번호와 비교
            if (!loginDto.getPassword().equals(user.getPassword())) {
                throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
            }
            
            // 평문 비밀번호를 BCrypt로 암호화하여 업데이트
            String encodedPassword = passwordEncoder.encode(loginDto.getPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }

        //  토큰 생성
        String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getId(), user.getIsAdmin() ? "ADMIN" : "USER");
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        //  RefreshToken 저장
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(user.getId())
                        .token(refreshToken)
                        .expiration(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000))
                        .build()
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    // 로그아웃 메서드 개선 - Access Token과 Refresh Token 모두 블랙리스트에 추가
    public void logout(Long userId, String accessToken, String refreshToken) {
        log.info(" [User Service] 로그아웃 처리 시작 - userId: {}", userId);
        
        try {
            // 1. RefreshToken 삭제
            refreshTokenRepository.deleteById(userId);
            log.info("[User Service] RefreshToken 삭제 완료 - userId: {}", userId);
            
            // 2. Access Token을 블랙리스트에 추가
            if (accessToken != null && !accessToken.isEmpty()) {
                tokenBlacklistService.blacklistToken(accessToken, jwtTokenProvider.getExpirationTime());
                log.info(" [User Service] Access Token 블랙리스트 추가 완료 - userId: {}", userId);
            }
            
            // 3. Refresh Token을 블랙리스트에 추가
            if (refreshToken != null && !refreshToken.isEmpty()) {
                tokenBlacklistService.blacklistRefreshToken(refreshToken, jwtTokenProvider.getRefreshExpiration());
                log.info(" [User Service] Refresh Token 블랙리스트 추가 완료 - userId: {}", userId);
            }
            
            // 4. 사용자 로그아웃 시간 기록
            tokenBlacklistService.recordUserLogout(userId, System.currentTimeMillis());
            log.info(" [User Service] 사용자 로그아웃 시간 기록 완료 - userId: {}", userId);
            
        } catch (Exception e) {
            log.error("❌[User Service] 로그아웃 처리 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    //  기존 로그아웃 메서드
    public void logout(Long userId) {
        logout(userId, null, null);
    }

    //  Access Token만 있는 경우
    public void logout(Long userId, String accessToken) {
        logout(userId, accessToken, null);
    }
}