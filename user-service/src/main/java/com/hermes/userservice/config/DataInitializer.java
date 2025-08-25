package com.hermes.userservice.config;

import com.hermes.userservice.entity.User;
import com.hermes.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("dev")
    public CommandLineRunner dataInitializer() {
        return args -> {
            log.info(" [Data Initializer] 테스트 데이터 초기화 시작");

            if (userRepository.count() == 0) {
                createTestUsers();
                log.info(" [Data Initializer] 테스트 데이터 생성 완료");
            } else {
                log.info(" [Data Initializer] 기존 데이터가 있어 테스트 데이터 생성 건너뜀");
            }
        };
    }

    private void createTestUsers() {

        User admin = new User();
        admin.setName("관리자");
        admin.setEmail("admin@hermes.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setPhone("010-1234-5678");
        admin.setAddress("서울시 강남구");
        admin.setJoinDate(LocalDate.of(2020, 1, 1));
        admin.setIsAdmin(true);
        userRepository.save(admin);


        User user = new User();
        user.setName("일반사용자");
        user.setEmail("user@hermes.com");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setPhone("010-9876-5432");
        user.setAddress("서울시 서초구");
        user.setJoinDate(LocalDate.of(2021, 3, 15));
        user.setIsAdmin(false);
        userRepository.save(user);

        log.info("테스트 사용자 생성 완료: admin@hermes.com, user@hermes.com");
    }
}
