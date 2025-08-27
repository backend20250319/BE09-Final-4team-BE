//package com.hermes.userservice.config;
//
//import com.hermes.userservice.entity.User;
//import com.hermes.userservice.repository.UserRepository;
//import com.hermes.auth.enums.Role;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DataInitializer implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        log.info("데이터 초기화 시작...");
//
//        // 테스트 사용자가 없으면 생성
//        if (userRepository.findByEmail("admin@hermes.com").isEmpty()) {
//            User adminUser = User.builder()
//                    .name("관리자")
//                    .email("admin@hermes.com")
//                    .password("admin123") // 실제로는 암호화 필요
//                    .phone("010-1234-5678")
//                    .address("서울시 강남구")
//                    .joinDate(LocalDate.now())
//                    .isAdmin(true)
//                    .role(Role.ADMIN)
//                    .build();
//
//            userRepository.save(adminUser);
//            log.info("관리자 사용자 생성 완료: {}", adminUser.getEmail());
//        }
//
//        if (userRepository.findByEmail("user@hermes.com").isEmpty()) {
//            User normalUser = User.builder()
//                    .name("일반사용자")
//                    .email("user@hermes.com")
//                    .password("user123") // 실제로는 암호화 필요
//                    .phone("010-9876-5432")
//                    .address("서울시 서초구")
//                    .joinDate(LocalDate.now())
//                    .isAdmin(false)
//                    .role(Role.USER)
//                    .build();
//
//            userRepository.save(normalUser);
//            log.info("일반 사용자 생성 완료: {}", normalUser.getEmail());
//        }
//
//        log.info("데이터 초기화 완료");
//    }
//}