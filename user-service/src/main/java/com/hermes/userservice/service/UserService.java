package com.hermes.userservice.service;

import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.repository.UserRepository;
import com.hermes.userservice.dto.UserAuthInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ auth-service에서 필요한 메서드만 유지
    public UserAuthInfo getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        return UserAuthInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .isAdmin(user.getIsAdmin())
                .build();
    }

    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        user.updateLastLogin();
        userRepository.save(user);
    }

    // 평문 비밀번호를 BCrypt로 변환하는 메소드
    public void updatePasswordToBcrypt(String email, String plainPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        String encodedPassword = passwordEncoder.encode(plainPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        log.info("비밀번호를 BCrypt로 업데이트 완료: {}", email);
    }

    // 애플리케이션 시작 시 자동으로 평문 비밀번호를 BCrypt로 변환
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void autoConvertPlainPasswordsOnStartup() {
        log.info("애플리케이션 시작 시 평문 비밀번호 자동 변환 시작");

        var users = userRepository.findAll();
        int convertedCount = 0;

        for (User user : users) {
            String password = user.getPassword();

            // BCrypt 형식이 아닌 경우 (평문으로 판단)
            if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                String encodedPassword = passwordEncoder.encode(password);
                user.setPassword(encodedPassword);
                userRepository.save(user);
                convertedCount++;
                log.info("사용자 {}의 비밀번호를 BCrypt로 변환 완료", user.getEmail());
            }
        }

        if (convertedCount > 0) {
            log.info("평문 비밀번호 자동 변환 완료: {}개 사용자 처리됨", convertedCount);
        } else {
            log.info("변환할 평문 비밀번호가 없습니다.");
        }
    }
}