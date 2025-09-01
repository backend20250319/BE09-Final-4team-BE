package com.hermes.userservice.service;

import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.InvalidCredentialsException;
import com.hermes.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSecurityService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void validatePassword(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }
    }

    @Transactional
    public void handleSuccessfulLogin(User user) {
        user.updateLastLogin();
        userRepository.save(user);
        log.info("Login successful for user: {}", user.getEmail());
    }

    @Transactional
    public void handleFailedLogin(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }
}