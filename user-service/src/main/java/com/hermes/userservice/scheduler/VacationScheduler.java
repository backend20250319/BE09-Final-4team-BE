package com.hermes.userservice.scheduler;

import com.hermes.userservice.service.VacationService;
import com.hermes.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VacationScheduler {

    private final VacationService vacationService;
    private final UserService userService;

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
    public void grantVacationOnAnniversaries() {
        log.info("연차 부여 스케줄러 실행 시작");
        
        try {
            int grantedCount = vacationService.grantVacationForAllAnniversaries();
            log.info("연차 부여 스케줄러 실행 완료: {}명에게 연차 부여", grantedCount);
        } catch (Exception e) {
            log.error("연차 부여 스케줄러 실행 실패", e);
        }
    }

    /**
     * 매일 자정에 모든 사용자의 근무년수를 업데이트
     * 연차 부여와 함께 근무년수도 최신 상태로 유지
     */
    @Scheduled(cron = "0 5 0 * * ?", zone = "Asia/Seoul") // 자정 5분 후 실행 (연차 부여 후)
    public void updateAllUsersWorkYears() {
        log.info("전체 사용자 근무년수 자동 업데이트 시작");
        
        try {
            userService.updateAllUsersWorkYears();
            log.info("전체 사용자 근무년수 자동 업데이트 완료");
        } catch (Exception e) {
            log.error("전체 사용자 근무년수 자동 업데이트 실패", e);
        }
    }
}
