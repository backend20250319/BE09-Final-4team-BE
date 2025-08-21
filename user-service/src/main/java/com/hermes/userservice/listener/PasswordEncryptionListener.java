package com.hermes.userservice.listener;

import com.hermes.userservice.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Slf4j
@Component
public class PasswordEncryptionListener {

    @Autowired
    private ApplicationContext applicationContext;

    @PrePersist
    @PreUpdate
    public void encryptPassword(User user) {
        if (user.getPassword() != null) {
            String password = user.getPassword();

            // BCrypt 형식이 아닌 경우 (평문으로 판단)
            if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
                String encodedPassword = passwordEncoder.encode(password);
                user.setPassword(encodedPassword);
                log.info("사용자 {}의 비밀번호를 자동으로 BCrypt로 암호화했습니다.", user.getEmail());
            }
        }
    }
}