package com.hermes.userservice.service;

import com.hermes.auth.JwtTokenProvider;
import com.hermes.auth.context.Role;
import com.hermes.auth.context.AuthContext;
import com.hermes.auth.context.UserInfo;
import com.hermes.userservice.dto.LoginRequest;
import com.hermes.userservice.dto.LoginResponse;
import com.hermes.userservice.dto.RegisterRequest;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.entity.RefreshToken;
import com.hermes.userservice.repository.UserRepository;
import com.hermes.userservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TokenCleanupService tokenCleanupService; 
    public User registerUser(RegisterRequest request) {
        log.info("사용자 등록 시작: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) 
                .name(request.getName())
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 등록 완료: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        return savedUser;
    }

    public LoginResponse loginUser(LoginRequest request) {
        log.info("사용자 로그인 시작: {}", request.getEmail());

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        User user = userOpt.get();

        log.debug("입력된 비밀번호: {}", request.getPassword());
        log.debug("저장된 비밀번호: {}", user.getPassword());
        log.debug("비밀번호 길이: {}", user.getPassword().length());

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.debug("비밀번호 일치 여부: {}", passwordMatches);

        if (!passwordMatches) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String userRole = user.getRole().toString();
        String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getId(), userRole);
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()), user.getEmail());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiration(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000))
                .build();
        
        refreshTokenRepository.save(refreshTokenEntity);

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("사용자 로그인 성공: {} (ID: {})", user.getEmail(), user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(userRole)
                .message("로그인이 성공적으로 완료되었습니다.")
                .build();
    }

    public void logoutUser(String token) {
        try {

            UserInfo userInfo = jwtTokenProvider.getUserInfoFromToken(token);
            if (userInfo != null) {
                log.info("사용자 {} 로그아웃 시작", userInfo.getEmail());
                
                List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userInfo.getUserId());
                log.info("삭제할 Refresh Token 개수: {}", tokens.size());
                
                refreshTokenRepository.deleteAllByUserId(userInfo.getUserId());
                log.info("사용자 {}의 모든 Refresh Token을 삭제했습니다.", userInfo.getEmail());
                
                tokenCleanupService.cleanupExpiredTokensByUserId(userInfo.getUserId());
            } else {
                log.warn("토큰에서 사용자 정보를 추출할 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        log.info("사용자 로그아웃 완료");
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}