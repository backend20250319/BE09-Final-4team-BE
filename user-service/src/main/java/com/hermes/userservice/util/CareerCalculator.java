package com.hermes.userservice.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@UtilityClass
@Slf4j
public class CareerCalculator {

    /**
     * 근무년수 계산 (입사일로부터 365일 경과 시 +1년)
     */
    public static int calculateCareerYears(LocalDate joinDate) {
        if (joinDate == null) {
            log.warn("입사일이 null입니다.");
            return 0;
        }
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(joinDate, today);
        int careerYears = (int) Math.max(0, days / 365);
        log.debug("경력년차 계산(365일 기준): 입사일={}, 오늘={}, 경력일수={}, 경력년차={}", joinDate, today, days, careerYears);
        return careerYears;
    }
}
