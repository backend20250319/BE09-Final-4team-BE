package com.hermes.userservice.service;

import com.hermes.userservice.client.WorkPolicyServiceClient;
import com.hermes.userservice.dto.workpolicy.WorkPolicyRequestDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyUpdateDto;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.repository.UserRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.stream.Collectors;
import com.hermes.api.common.ApiResult;
import org.springframework.stereotype.Component;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkPolicyIntegrationService {

    private final WorkPolicyServiceClient workPolicyServiceClient;
    private final UserRepository userRepository;

    public WorkPolicyResponseDto getUserWorkPolicy(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        Long workPolicyId = user.getWorkPolicyId();
        if (workPolicyId == null) {
            log.info("사용자에게 설정된 근무 정책이 없습니다: userId={}", userId);
            return null;
        }

        return getWorkPolicy(workPolicyId);
    }

    public WorkPolicyResponseDto getWorkPolicy(Long workPolicyId) {
        log.info("근무 정책 조회: workPolicyId={}", workPolicyId);
        try {
            ApiResult<WorkPolicyResponseDto> result = workPolicyServiceClient.getWorkPolicy(workPolicyId);
            if ("SUCCESS".equals(result.getStatus())) {
                return result.getData();
            } else {
                log.error("근무 정책 조회 실패: {}", result.getMessage());
                return null;
            }
        } catch (FeignException.NotFound e) {
            log.error("근무 정책을 찾을 수 없습니다. workPolicyId={}", workPolicyId, e);
            return null;
        } catch (FeignException e) {
            log.warn("근무 정책 서비스가 사용 불가능합니다. workPolicyId={}, status={}", workPolicyId, e.status());
            return null; 
        }
    }

    public WorkPolicyResponseDto createWorkPolicy(WorkPolicyRequestDto request) {
        log.info("근무 정책 생성 요청: {}", request);
        try {
            ApiResult<WorkPolicyResponseDto> result = workPolicyServiceClient.createWorkPolicy(request);
            if ("SUCCESS".equals(result.getStatus())) {
                return result.getData();
            } else {
                log.error("근무 정책 생성 실패: {}", result.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("근무 정책 생성 중 오류 발생", e);
            return null;
        }
    }

    public WorkPolicyResponseDto updateWorkPolicy(Long workPolicyId, WorkPolicyUpdateDto request) {
        log.info("근무 정책 업데이트 요청: workPolicyId={}", workPolicyId);
        try {
            ApiResult<WorkPolicyResponseDto> result = workPolicyServiceClient.updateWorkPolicy(workPolicyId, request);
            if ("SUCCESS".equals(result.getStatus())) {
                return result.getData();
            } else {
                log.error("근무 정책 업데이트 실패: {}", result.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("근무 정책 업데이트 중 오류 발생", e);
            return null;
        }
    }

    public void deleteWorkPolicy(Long workPolicyId) {
        log.info("근무 정책 삭제 요청: workPolicyId={}", workPolicyId);
        try {
            ApiResult<Void> result = workPolicyServiceClient.deleteWorkPolicy(workPolicyId);
            if ("SUCCESS".equals(result.getStatus())) {
                log.info("근무 정책 삭제 성공: workPolicyId={}", workPolicyId);
            } else {
                log.error("근무 정책 삭제 실패: {}", result.getMessage());
            }
        } catch (FeignException.NotFound e) {
            log.warn("삭제하려는 근무 정책을 찾을 수 없습니다. workPolicyId={}", workPolicyId, e);
        } catch (FeignException e) {
            log.error("근무 정책 삭제 중 FeignClient 오류 발생: workPolicyId={}, status={}", workPolicyId, e.status(), e);
            throw new RuntimeException("근무 정책 삭제 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersWithPagination(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByNameContaining(String name) {
        return userRepository.findByNameContaining(name);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByEmailContaining(String email) {
        return userRepository.findByEmailContaining(email);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByAdminStatus(Boolean isAdmin) {
        return userRepository.findByIsAdmin(isAdmin);
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(String name, String email, Pageable pageable) {
        return userRepository.findByNameContainingOrEmailContaining(name, email, pageable);
    }
}