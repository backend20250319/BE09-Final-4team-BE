package com.hermes.userservice.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@UtilityClass
@Slf4j
public class CareerCalculator {

    public static long calculateCareerDays(LocalDate joinDate) {
        if (joinDate == null) {
            log.warn("입사일이 null입니다.");
            return 0;
        }
        
        LocalDate today = LocalDate.now();
        long careerDays = ChronoUnit.DAYS.between(joinDate, today);
        
        log.debug("경력일수 계산: 입사일={}, 오늘={}, 경력일수={}", joinDate, today, careerDays);
        return Math.max(0, careerDays);
    }

    public static int calculateCareerYears(LocalDate joinDate) {
        if (joinDate == null) {
            log.warn("입사일이 null입니다.");
            return 0;
        }
        
        LocalDate today = LocalDate.now();
        int careerYears = (int) ChronoUnit.YEARS.between(joinDate, today);
        
        log.debug("경력년차 계산: 입사일={}, 오늘={}, 경력년차={}", joinDate, today, careerYears);
        return Math.max(0, careerYears);
    }

    public static long calculateCareerMonths(LocalDate joinDate) {
        if (joinDate == null) {
            log.warn("입사일이 null입니다.");
            return 0;
        }
        
        LocalDate today = LocalDate.now();
        long careerMonths = ChronoUnit.MONTHS.between(joinDate, today);
        
        log.debug("경력개월수 계산: 입사일={}, 오늘={}, 경력개월수={}", joinDate, today, careerMonths);
        return Math.max(0, careerMonths);
    }

    public static boolean isAnniversary(LocalDate joinDate) {
        if (joinDate == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        boolean isAnniversary = joinDate.getMonth() == today.getMonth() && 
                               joinDate.getDayOfMonth() == today.getDayOfMonth();
        
        log.debug("입사일 확인: 입사일={}, 오늘={}, 입사일={}", joinDate, today, isAnniversary);
        return isAnniversary;
    }

    public static int calculateBasicLeaveDays(int careerYears) {
        if (careerYears < 0) {
            return 0;
        }
        
        int leaveDays;
        if (careerYears == 0) {
            leaveDays = 11;
        } else if (careerYears < 2) {
            leaveDays = 15;
        } else if (careerYears < 3) {
            leaveDays = 16;
        } else if (careerYears < 4) {
            leaveDays = 18;
        } else if (careerYears < 5) {
            leaveDays = 20;
        } else if (careerYears < 6) {
            leaveDays = 21;
        } else if (careerYears < 7) {
            leaveDays = 22;
        } else if (careerYears < 8) {
            leaveDays = 23;
        } else if (careerYears < 9) {
            leaveDays = 24;
        } else if (careerYears < 10) {
            leaveDays = 25;
        } else {
            leaveDays = 25;
        }
        
        log.debug("기본 휴가 일수 계산: 경력년차={}, 휴가일수={}", careerYears, leaveDays);
        return leaveDays;
    }
}
