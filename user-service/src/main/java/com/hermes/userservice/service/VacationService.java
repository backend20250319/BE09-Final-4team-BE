package com.hermes.userservice.service;

import com.hermes.userservice.dto.workpolicy.AnnualLeaveResponseDto;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.repository.UserRepository;
import com.hermes.userservice.util.CareerCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VacationService {

    private final UserRepository userRepository;
    private final WorkPolicyIntegrationService workPolicyIntegrationService;

    public int grantVacationOnAnniversary(Long userId) {
        log.info("연차 부여 시작: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        LocalDate joinDate = user.getJoinDate();
        if (joinDate == null) {
            log.warn("입사일이 null입니다: userId={}", userId);
            return 0;
        }
        
        if (!CareerCalculator.isAnniversary(joinDate)) {
            log.info("입사일이 아닙니다: userId={}, joinDate={}", userId, joinDate);
            return 0;
        }
        
        int careerYears = CareerCalculator.calculateCareerYears(joinDate);
        
        // 근무년수 업데이트 (연차 부여와 함께)
        user.updateWorkYears(careerYears);
        userRepository.save(user);
        log.info("근무년수 업데이트 완료: userId={}, workYears={}", userId, careerYears);
        
        int leaveDays = getLeaveDaysByCareerYears(user.getWorkPolicyId(), careerYears);
        
        if (leaveDays > 0) {
            log.info("연차 부여 완료: userId={}, careerYears={}, leaveDays={}", userId, careerYears, leaveDays);
        } else {
            log.info("부여할 연차가 없습니다: userId={}, careerYears={}", userId, careerYears);
        }
        
        return leaveDays;
    }

    public int grantVacationForAllAnniversaries() {
        log.info("전체 사용자 연차 부여 시작");
        
        List<User> allUsers = userRepository.findAll();
        int grantedCount = 0;
        
        for (User user : allUsers) {
            try {
                int leaveDays = grantVacationOnAnniversary(user.getId());
                if (leaveDays > 0) {
                    grantedCount++;
                }
            } catch (Exception e) {
                log.error("사용자 연차 부여 실패: userId={}", user.getId(), e);
            }
        }
        
        log.info("전체 사용자 연차 부여 완료: 총 {}명에게 연차 부여", grantedCount);
        return grantedCount;
    }

    private int getLeaveDaysByCareerYears(Long workPolicyId, int careerYears) {
        if (workPolicyId == null) {
            log.warn("근무정책 ID가 null입니다. 기본 휴가 일수 사용: careerYears={}", careerYears);
            return CareerCalculator.calculateBasicLeaveDays(careerYears);
        }
        
        List<AnnualLeaveResponseDto> annualLeaves = workPolicyIntegrationService.getAnnualLeavesByWorkPolicyId(workPolicyId);
        
        if (annualLeaves == null || annualLeaves.isEmpty()) {
            log.warn("연차 정책을 찾을 수 없습니다: workPolicyId={}, careerYears={}", workPolicyId, careerYears);
            return CareerCalculator.calculateBasicLeaveDays(careerYears);
        }
        
        for (AnnualLeaveResponseDto annualLeave : annualLeaves) {
            if (annualLeave.getMinYears() <= careerYears && careerYears <= annualLeave.getMaxYears()) {
                log.debug("휴가 정책 매칭: careerYears={}, leaveDays={}", careerYears, annualLeave.getLeaveDays());
                return annualLeave.getLeaveDays();
            }
        }
        
        log.warn("경력년차에 맞는 휴가 정책을 찾을 수 없습니다: workPolicyId={}, careerYears={}", workPolicyId, careerYears);
        return CareerCalculator.calculateBasicLeaveDays(careerYears);
    }
}
