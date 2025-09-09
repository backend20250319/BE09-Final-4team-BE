package com.hermes.userservice.service;

import com.hermes.userservice.entity.User;
import com.hermes.userservice.repository.UserRepository;
import com.hermes.userservice.util.CareerCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VacationService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public int getUserWorkYears(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        if (user.getWorkYears() != null) {
            return user.getWorkYears();
        }
        int calculatedWorkYears = CareerCalculator.calculateCareerYears(user.getJoinDate());
        updateWorkYearsAsync(userId, calculatedWorkYears);
        return calculatedWorkYears;
    }

    public void updateWorkYears(Long userId) {
        log.info("근무년수 업데이트: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        int workYears = CareerCalculator.calculateCareerYears(user.getJoinDate());
        user.updateWorkYears(workYears);
        userRepository.save(user);
    }

    public void updateAllUsersWorkYears() {
        log.info("전체 사용자 근무년수 업데이트 시작");
        userRepository.findAll().forEach(user -> {
            try {
                int workYears = CareerCalculator.calculateCareerYears(user.getJoinDate());
                user.updateWorkYears(workYears);
                userRepository.save(user);
            } catch (Exception e) {
                log.error("사용자 근무년수 업데이트 실패: userId={}", user.getId(), e);
            }
        });
        log.info("전체 사용자 근무년수 업데이트 완료");
    }

    @Transactional
    public void updateWorkYearsAsync(Long userId, int workYears) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getWorkYears() == null) {
                user.updateWorkYears(workYears);
                userRepository.save(user);
                log.info("workYears 자동 설정: userId={}, workYears={}", userId, workYears);
            }
        } catch (Exception e) {
            log.warn("workYears 자동 설정 실패: userId={}, workYears={}", userId, workYears, e);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
    public void scheduledUpdateAllUsersWorkYears() {
        log.info("스케줄러(00:00) 근무년수 일괄 업데이트 시작");
        updateAllUsersWorkYears();
        log.info("스케줄러(00:00) 근무년수 일괄 업데이트 완료");
    }
}
