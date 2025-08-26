package com.hermes.userservice.service;

import com.hermes.auth.enums.Role;
import com.hermes.auth.dto.TokenResponse;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.dto.UserCreateDto;
import com.hermes.userservice.dto.UserResponseDto;
import com.hermes.userservice.dto.UserUpdateDto;
import com.hermes.userservice.entity.RefreshToken;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.DuplicateEmailException;
import com.hermes.userservice.exception.InvalidCredentialsException;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.mapper.UserMapper;
import com.hermes.userservice.repository.RefreshTokenRepository;
import com.hermes.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserMapper userMapper;

    public TokenResponse login(LoginRequestDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        user.updateLastLogin();
        userRepository.save(user);

        Role userRole = user.getIsAdmin() ? Role.ADMIN : Role.USER;
        String accessToken = jwtTokenService.createAccessToken(user.getEmail(), user.getId(), userRole, null);
        String refreshToken = jwtTokenService.createRefreshToken(String.valueOf(user.getId()), user.getEmail());

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(user.getId())
                        .token(refreshToken)
                        .expiration(LocalDateTime.now().plusSeconds(jwtTokenService.getRefreshTokenExpiration() / 1000))
                        .build()
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    public void logout(Long userId, String accessToken, String refreshToken) {
        log.info("[User Service] 로그아웃 처리 시작 - userId: {}", userId);

        try {
            refreshTokenRepository.deleteById(userId);
            tokenBlacklistService.logoutUser(userId, accessToken, refreshToken);
            log.info("[User Service] 모든 토큰 완전 삭제 완료 - userId: {}", userId);
        } catch (Exception e) {
            log.error("[User Service] 로그아웃 처리 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
    }

    public User createUser(UserCreateDto userCreateDto) {
        if (userRepository.findByEmail(userCreateDto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + userCreateDto.getEmail());
        }

        User user = userMapper.toEntity(userCreateDto);
        return userRepository.save(user);
    }

    public User updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User user = getUserById(userId);

        if (userUpdateDto.getEmail() != null && !Objects.equals(user.getEmail(), userUpdateDto.getEmail())) {
            if (userRepository.findByEmail(userUpdateDto.getEmail()).isPresent()) {
                throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + userUpdateDto.getEmail());
            }
            user.updateEmail(userUpdateDto.getEmail());
        }

        if (userUpdateDto.getPassword() != null) {
            user.updatePassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }

        if (userUpdateDto.getName() != null) {
            user.updateName(userUpdateDto.getName());
        }
        if (userUpdateDto.getPhone() != null) {
            user.updatePhone(userUpdateDto.getPhone());
        }
        if (userUpdateDto.getAddress() != null) {
            user.updateAddress(userUpdateDto.getAddress());
        }
        if (userUpdateDto.getProfileImageUrl() != null) {
            user.updateProfileImageUrl(userUpdateDto.getProfileImageUrl());
        }
        if (userUpdateDto.getSelfIntroduction() != null) {
            user.updateSelfIntroduction(userUpdateDto.getSelfIntroduction());
        }

        if (userUpdateDto.getEmploymentType() != null) {
            user.updateEmploymentType(userUpdateDto.getEmploymentType());
        }
        if (userUpdateDto.getRank() != null) {
            user.updateRank(userUpdateDto.getRank());
        }
        if (userUpdateDto.getPosition() != null) {
            user.updatePosition(userUpdateDto.getPosition());
        }
        if (userUpdateDto.getJob() != null) {
            user.updateJob(userUpdateDto.getJob());
        }
        if (userUpdateDto.getRole() != null) {
            user.updateRole(userUpdateDto.getRole());
        }
        if (userUpdateDto.getWorkPolicyId() != null) {
            user.updateWorkPolicyId(userUpdateDto.getWorkPolicyId());
        }

        if (userUpdateDto.getIsAdmin() != null) {
            user.updateAdminStatus(userUpdateDto.getIsAdmin());
        }
        if (userUpdateDto.getNeedsPasswordReset() != null) {
            user.updatePasswordResetFlag(userUpdateDto.getNeedsPasswordReset());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("삭제할 사용자를 찾을 수 없습니다: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("사용자 삭제 완료: userId={}", userId);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Deprecated
    public UserResponseDto convertToUserResponseDto(User user) {
        return userMapper.toResponseDto(user);
    }

    public User updateUserWorkPolicy(Long userId, Long workPolicyId) {
        User user = getUserById(userId);
        user.updateWorkPolicyId(workPolicyId);
        return userRepository.save(user);
    }
}