package com.hermes.userservice.service;

import com.hermes.auth.JwtTokenProvider;
import com.hermes.auth.context.Role;
import com.hermes.auth.context.UserInfo;
import com.hermes.auth.service.TokenBlacklistService;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.InvalidCredentialsException;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.auth.dto.TokenResponse;
import com.hermes.userservice.entity.RefreshToken;
import com.hermes.userservice.repository.RefreshTokenRepository;
import com.hermes.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            // 기존 평문 비밀번호와 비교
            if (!loginDto.getPassword().equals(user.getPassword())) {
                throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
            }

            String encodedPassword = passwordEncoder.encode(loginDto.getPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }

        Role userRole = user.getIsAdmin() ? Role.ADMIN : Role.USER;
        String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getId(), userRole);
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()), user.getEmail());

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(user.getId())
                        .token(refreshToken)
                        .expiration(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000))
                        .build()
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    public void logoutUser(String token) {
        log.info("로그아웃 시작 - 토큰: {}", token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");
        
        try {
            UserInfo userInfo = jwtTokenProvider.getUserInfoFromToken(token);
            if (userInfo != null) {
                log.info("사용자 정보 추출 성공: {} (ID: {})", userInfo.getEmail(), userInfo.getUserId());
                
                Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByUserId(userInfo.getUserId());
                if (tokenOpt.isPresent()) {
                    refreshTokenRepository.delete(tokenOpt.get());
                    log.info("사용자 {}의 Refresh Token을 삭제했습니다.", userInfo.getEmail());
                }
                
                tokenBlacklistService.logoutUser(userInfo.getUserId(), token, null);
                log.info("사용자 {}의 Access Token을 블랙리스트에 추가했습니다.", userInfo.getEmail());
                
            } else {
                log.warn("토큰에서 사용자 정보를 추출할 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        log.info("사용자 로그아웃 완료");
    }

    public void logout(Long userId, String accessToken, String refreshToken) {
        log.info(" [User Service] 로그아웃 처리 시작 - userId: {}", userId);

        try {

            refreshTokenRepository.deleteById(userId);
            log.info("[User Service] RefreshToken 삭제 완료 - userId: {}", userId);

            tokenBlacklistService.logoutUser(userId, accessToken, refreshToken);
            log.info(" [User Service] 모든 토큰 완전 삭제 완료 - userId: {}", userId);

        } catch (Exception e) {
            log.error("[User Service] 로그아웃 처리 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    public void logout(Long userId) {
        logout(userId, null, null);
    }

    public void logout(Long userId, String accessToken) {
        logout(userId, accessToken, null);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
    }

    public User updateUserWorkPolicy(Long userId, Long workPolicyId) {
        User user = getUserById(userId);
        user.setWorkPolicyId(workPolicyId);
        return userRepository.save(user);
    }

    public User createUser(String name, String email, String password, Long workPolicyId) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다: " + email);
        }
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setWorkPolicyId(workPolicyId);
        user.setIsAdmin(false); // 기본값은 일반 사용자임 ㅎㅎㅎ
        
        return userRepository.save(user);
    }
}