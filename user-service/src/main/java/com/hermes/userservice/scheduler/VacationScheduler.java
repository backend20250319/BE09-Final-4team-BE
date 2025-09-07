package com.hermes.userservice.scheduler;

import com.hermes.userservice.service.VacationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VacationScheduler {

    private final VacationService vacationService;

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
}
