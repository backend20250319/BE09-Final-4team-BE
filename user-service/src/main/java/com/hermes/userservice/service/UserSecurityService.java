package com.hermes.userservice.service;

import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.InvalidCredentialsException;
import com.hermes.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSecurityService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//    @Value("${security.login.max-attempts:5}")
//    private int maxLoginAttempts;

//    @Value("${security.login.lock-duration-minutes:30}")
//    private int lockDurationMinutes;

// 계정 상태 검증

//    public void validateAccountStatus(User user) {
//        if (user.getIsLocked()) {
//            throw new InvalidCredentialsException("비활성화된 계정입니다.");
//        }
//
//        if (user.isAccountLocked() && isAccountStillLocked(user)) {
//            throw new InvalidCredentialsException(
//                String.format("계정이 잠겨있습니다. %d분 후에 다시 시도해주세요.", lockDurationMinutes));
//        }
//    }

// 비밀번호 검증

    public void validatePassword(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }
    }

// 로그인 성공 처리

    @Transactional
    public void handleSuccessfulLogin(User user) {
        user.updateLastLogin();
//        user.resetLoginAttempts();
        userRepository.save(user);
        log.info("Login successful for user: {}", user.getEmail());
    }

// 로그인 실패 처리

    @Transactional
    public void handleFailedLogin(User user) {
//        user.incrementLoginAttempts();

//        if (user.getLoginAttempts() >= maxLoginAttempts) {
//            user.lockAccount();
//            log.warn("Account locked for user: {} due to {} failed attempts",
//                    user.getEmail(), user.getLoginAttempts());
//        }

        userRepository.save(user);
//        log.warn("Login failed for user: {} - Attempt {}/{}",
//                user.getEmail(), user.getLoginAttempts(), maxLoginAttempts);
    }

// 계정 잠금 해제

//    @Transactional
//    public void unlockAccount(User user) {
//        user.unlockAccount();
//        userRepository.save(user);
//        log.info("Account unlocked for user: {}", user.getEmail());
//    }

// 계정이 아직 잠겨있는지 확인

//    private boolean isAccountStillLocked(User user) {
//        return user.getLockedAt() != null &&
//               LocalDateTime.now().isBefore(user.getLockedAt().plusMinutes(lockDurationMinutes));
//    }

// 계정 상태 토글

//    @Transactional
//    public void toggleAccountStatus(User user, boolean isActive) {
//        user.setIsLocked(!isActive);
//        userRepository.save(user);
//        log.info("Account status changed for user: {} - Active: {}", user.getEmail(), isActive);
//    }

// 비밀번호 변경

    @Transactional
    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }
}
