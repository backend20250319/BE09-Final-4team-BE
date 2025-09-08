package com.hermes.userservice.service;

import com.hermes.userservice.dto.*;
import com.hermes.userservice.dto.title.*;
import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.entity.EmploymentType;
import com.hermes.userservice.entity.Rank;
import com.hermes.userservice.entity.Position;
import com.hermes.userservice.entity.Job;
import com.hermes.userservice.exception.DuplicateEmailException;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.mapper.UserMapper;
import com.hermes.userservice.repository.*;
import com.hermes.userservice.util.CareerCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final OrganizationIntegrationService organizationIntegrationService;
    private final WorkPolicyIntegrationService workPolicyIntegrationService;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final RankRepository rankRepository;
    private final PositionRepository positionRepository;
    private final JobRepository jobRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("애플리케이션 시작 시 근무년수가 null인 사용자들을 초기화합니다.");
        List<User> usersWithNullWorkYears = userRepository.findByWorkYearsIsNull();
        int initializedCount = 0;
        for (User user : usersWithNullWorkYears) {
            try {
                int workYears = CareerCalculator.calculateCareerYears(user.getJoinDate());
                user.updateWorkYears(workYears);
                userRepository.save(user);
                initializedCount++;
                log.debug("근무년수 초기화: userId={}, workYears={}", user.getId(), workYears);
            } catch (Exception e) {
                log.error("사용자 근무년수 초기화 실패: userId={}", user.getId(), e);
            }
        }
        log.info("애플리케이션 시작 시 근무년수 초기화 완료: 총 {}명 초기화", initializedCount);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        log.info("사용자 상세 조회 요청 (근무정책 및 조직 정보 포함): userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        List<Map<String, Object>> remoteOrganizations = organizationIntegrationService.getUserOrganizations(userId);

        WorkPolicyResponseDto workPolicy = null;
        if (user.getWorkPolicyId() != null) {
            try {
                workPolicy = workPolicyIntegrationService.getWorkPolicyById(user.getWorkPolicyId());
            } catch (Exception e) {
                log.warn("근무 정책 조회 실패, null로 처리: userId={}, workPolicyId={}", userId, user.getWorkPolicyId(), e);
            }
        }

        return userMapper.toResponseDto(user, remoteOrganizations, workPolicy);
    }

    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        log.info("createUser 호출: userCreateDto={}", userCreateDto);
        
        if (userRepository.findByEmail(userCreateDto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + userCreateDto.getEmail());
        }

        User user = User.builder()
                .name(userCreateDto.getName())
                .email(userCreateDto.getEmail())
                .password(passwordEncoder.encode(userCreateDto.getPassword()))
                .phone(userCreateDto.getPhone())
                .address(userCreateDto.getAddress())
                .joinDate(Optional.ofNullable(userCreateDto.getJoinDate()).orElse(LocalDate.now()))
                .isAdmin(Optional.ofNullable(userCreateDto.getIsAdmin()).orElse(false))
                .needsPasswordReset(Optional.ofNullable(userCreateDto.getNeedsPasswordReset()).orElse(false))
                .role(userCreateDto.getRole())
                .workPolicyId(userCreateDto.getWorkPolicyId())
                .build();

        User createdUser = userRepository.save(user);
        log.info("User 저장 완료: userId={}", createdUser.getId());

        EmploymentType employmentType = null;
        Rank rank = null;
        Position position = null;
        Job job = null;

        if (userCreateDto.getEmploymentType() != null && userCreateDto.getEmploymentType().getId() != null && userCreateDto.getEmploymentType().getId() != 0) {
            employmentType = employmentTypeRepository.findById(userCreateDto.getEmploymentType().getId()).orElse(null);
            log.info("EmploymentType 조회: id={}, result={}", userCreateDto.getEmploymentType().getId(), employmentType);
        }

        if (userCreateDto.getRank() != null && userCreateDto.getRank().getId() != null && userCreateDto.getRank().getId() != 0) {
            rank = rankRepository.findById(userCreateDto.getRank().getId()).orElse(null);
            log.info("Rank 조회: id={}, result={}", userCreateDto.getRank().getId(), rank);
        }

        if (userCreateDto.getPosition() != null && userCreateDto.getPosition().getId() != null && userCreateDto.getPosition().getId() != 0) {
            position = positionRepository.findById(userCreateDto.getPosition().getId()).orElse(null);
            log.info("Position 조회: id={}, result={}", userCreateDto.getPosition().getId(), position);
        }

        if (userCreateDto.getJob() != null && userCreateDto.getJob().getId() != null && userCreateDto.getJob().getId() != 0) {
            job = jobRepository.findById(userCreateDto.getJob().getId()).orElse(null);
            log.info("Job 조회: id={}, result={}", userCreateDto.getJob().getId(), job);
        }

        // updateWorkInfo 메서드를 사용해서 rank, position, job 설정
        createdUser.updateWorkInfo(employmentType, rank, position, job, userCreateDto.getRole(), userCreateDto.getWorkPolicyId());
        log.info("updateWorkInfo 완료: rank={}, position={}, job={}", createdUser.getRank(), createdUser.getPosition(), createdUser.getJob());

        User finalUser = userRepository.save(createdUser);
        log.info("최종 저장 완료: userId={}, rank={}, position={}, job={}", finalUser.getId(), finalUser.getRank(), finalUser.getPosition(), finalUser.getJob());

        List<Map<String, Object>> remoteOrganizations = organizationIntegrationService.getUserOrganizations(createdUser.getId());

        WorkPolicyResponseDto workPolicy = null;
        if (createdUser.getWorkPolicyId() != null) {
            try {
                workPolicy = workPolicyIntegrationService.getWorkPolicyById(createdUser.getWorkPolicyId());
            } catch (Exception e) {
                log.warn("근무 정책 조회 실패, null로 처리: userId={}, workPolicyId={}", createdUser.getId(), createdUser.getWorkPolicyId(), e);
            }
        }

        return userMapper.toResponseDto(finalUser, remoteOrganizations, workPolicy);
    }

    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        log.info("사용자 업데이트 시작: userId={}, updateData={}", userId, userUpdateDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        log.info("업데이트 전 사용자 데이터: name={}, phone={}, address={}, joinDate={}",
                user.getName(), user.getPhone(), user.getAddress(), user.getJoinDate());

        if (userUpdateDto.getEmail() != null && !Objects.equals(user.getEmail(), userUpdateDto.getEmail())) {
            if (userRepository.findByEmail(userUpdateDto.getEmail()).isPresent()) {
                throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + userUpdateDto.getEmail());
            }
            user.updateEmail(userUpdateDto.getEmail());
        }
        if (userUpdateDto.getPassword() != null) {
            user.updatePassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }

        if (userUpdateDto.getJoinDate() != null) {
            user.updateJoinDate(userUpdateDto.getJoinDate());
        }

        user.updateInfo(userUpdateDto.getName(), userUpdateDto.getPhone(), userUpdateDto.getAddress(), userUpdateDto.getProfileImageUrl(), userUpdateDto.getSelfIntroduction());
        user.updateWorkInfo(
            userUpdateDto.getEmploymentType() != null && userUpdateDto.getEmploymentType().getId() != null && userUpdateDto.getEmploymentType().getId() != 0 ? 
                employmentTypeRepository.findById(userUpdateDto.getEmploymentType().getId()).orElse(null) : null,
            userUpdateDto.getRank() != null && userUpdateDto.getRank().getId() != null && userUpdateDto.getRank().getId() != 0 ? 
                rankRepository.findById(userUpdateDto.getRank().getId()).orElse(null) : null,
            userUpdateDto.getPosition() != null && userUpdateDto.getPosition().getId() != null && userUpdateDto.getPosition().getId() != 0 ? 
                positionRepository.findById(userUpdateDto.getPosition().getId()).orElse(null) : null,
            userUpdateDto.getJob() != null && userUpdateDto.getJob().getId() != null && userUpdateDto.getJob().getId() != 0 ? 
                jobRepository.findById(userUpdateDto.getJob().getId()).orElse(null) : null,
            userUpdateDto.getRole(), 
            userUpdateDto.getWorkPolicyId()
        );
        user.updateAdminStatus(userUpdateDto.getIsAdmin());
        user.updatePasswordResetFlag(userUpdateDto.getNeedsPasswordReset());

        log.info("업데이트 후 사용자 데이터: name={}, phone={}, address={}, joinDate={}",
                user.getName(), user.getPhone(), user.getAddress(), user.getJoinDate());

        User updatedUser = userRepository.save(user);

        log.info("DB 저장 완료: userId={}", updatedUser.getId());

        List<Map<String, Object>> remoteOrganizations = organizationIntegrationService.getUserOrganizations(updatedUser.getId());

        WorkPolicyResponseDto workPolicy = null;
        if (updatedUser.getWorkPolicyId() != null) {
            try {
                workPolicy = workPolicyIntegrationService.getWorkPolicyById(updatedUser.getWorkPolicyId());
            } catch (Exception e) {
                log.warn("근무 정책 조회 실패: {}", e.getMessage());
            }
        }

        UserResponseDto response = userMapper.toResponseDto(updatedUser, remoteOrganizations, workPolicy);
        log.info("응답 데이터 생성 완료: userId={}", response.getId());

        return response;
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("삭제할 사용자를 찾을 수 없습니다: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("사용자 삭제 완료: userId={}", userId);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        log.info("전체 사용자 목록 조회 요청 (근무정책 및 조직 정보 포함)");
        List<User> users = userRepository.findAll();

        Map<Long, List<Map<String, Object>>> allOrganizations = organizationIntegrationService.getAllUsersOrganizations();

        List<UserResponseDto> result = users.stream()
                .map(user -> {
                    List<Map<String, Object>> userOrganizations = allOrganizations.getOrDefault(user.getId(), List.of());

                    WorkPolicyResponseDto workPolicy = null;
                    if (user.getWorkPolicyId() != null) {
                        try {
                            workPolicy = workPolicyIntegrationService.getWorkPolicyById(user.getWorkPolicyId());
                        } catch (Exception e) {
                            log.warn("근무 정책 조회 실패, null로 처리: userId={}, workPolicyId={}", user.getId(), user.getWorkPolicyId(), e);
                        }
                    }

                    return userMapper.toResponseDto(user, userOrganizations, workPolicy);
                })
                .collect(Collectors.toList());
        
        return result;
    }

    public User updateUserWorkPolicy(Long userId, Long workPolicyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        user.updateWorkPolicyId(workPolicyId);
        return userRepository.save(user);
    }
    @Transactional(readOnly = true)
    public MainProfileResponseDto getMainProfile(Long userId) {
        log.info("공개 프로필 조회 요청: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        log.info("사용자 정보: userId={}, workPolicyId={}", userId, user.getWorkPolicyId());

        // 근무정책 정보 조회 추가
        WorkPolicyResponseDto workPolicy = null;
        if (user.getWorkPolicyId() != null) {
            try {
                log.info("근무정책 조회 시도: userId={}, workPolicyId={}", userId, user.getWorkPolicyId());
                workPolicy = workPolicyIntegrationService.getWorkPolicyById(user.getWorkPolicyId());
                log.info("근무정책 조회 결과: userId={}, workPolicy={}", userId, workPolicy);
            } catch (Exception e) {
                log.warn("근무 정책 조회 실패, null로 처리: userId={}, workPolicyId={}", userId, user.getWorkPolicyId(), e);
            }
        } else {
            log.info("사용자에게 근무정책 ID가 설정되지 않음: userId={}", userId);
        }

        MainProfileResponseDto result = userMapper.toMainProfileDto(user, workPolicy);
        log.info("최종 응답: userId={}, workPolicy={}", userId, result.getWorkPolicy());
        return result;
    }

    @Transactional(readOnly = true)
    public DetailProfileResponseDto getDetailProfile(Long userId) {
        log.info("상세 프로필 조회 요청: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        return userMapper.toDetailProfileDto(user);
    }

    @Transactional(readOnly = true)
    public List<Long> searchUserIdsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        
        List<User> users = userRepository.findByNameContaining(name.trim());
        return users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ColleagueResponseDto> getColleagues(ColleagueSearchRequestDto searchRequest) {
        log.info("동료 목록 조회 요청: searchKeyword={}, department={}, position={}",
                searchRequest.getSearchKeyword(), searchRequest.getDepartment(), searchRequest.getPosition());

        List<User> users = userRepository.findAll();

        return users.stream()
                .filter(user -> {
                    if (searchRequest.getSearchKeyword() != null && !searchRequest.getSearchKeyword().trim().isEmpty()) {
                        String keyword = searchRequest.getSearchKeyword().toLowerCase();
                        return user.getName().toLowerCase().contains(keyword) ||
                                (user.getPosition() != null && user.getPosition().getName().toLowerCase().contains(keyword)) ||
                                (user.getEmail() != null && user.getEmail().toLowerCase().contains(keyword));
                    }
                    return true;
                })
                .filter(user -> {
                    if (searchRequest.getDepartment() != null && !searchRequest.getDepartment().trim().isEmpty()) {
                        return true;
                    }
                    return true;
                })
                .filter(user -> {
                    if (searchRequest.getPosition() != null && !searchRequest.getPosition().trim().isEmpty()) {
                        return user.getPosition() != null &&
                                user.getPosition().getName().toLowerCase().contains(searchRequest.getPosition().toLowerCase());
                    }
                    return true;
                })
                .map(user -> ColleagueResponseDto.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhone())
                        .avatar(user.getProfileImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getTotalEmployees() {
        log.info("전체 직원 수 조회");
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Long> getAllUserIds() {
        log.info("전체 사용자 ID 목록 조회 (알림 발송용)");
        return userRepository.findAllUserIds();
    }

    @Transactional
    public void updateProfileImage(Long userId, String profileImageUrl) {
        log.info("프로필 이미지 업데이트: userId={}, imageUrl={}", userId, profileImageUrl);

        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        // 프로필 이미지 URL만 업데이트
        userRepository.updateProfileImageUrl(userId, profileImageUrl);

        log.info("프로필 이미지 업데이트 완료: userId={}", userId);
    }

    /**
     * 특정 사용자의 근무년수를 계산하여 업데이트
     */
    public void updateWorkYears(Long userId) {
        log.info("근무년수 업데이트 시작: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        int workYears = CareerCalculator.calculateCareerYears(user.getJoinDate());
        user.updateWorkYears(workYears);
        
        userRepository.save(user);
        
        log.info("근무년수 업데이트 완료: userId={}, workYears={}", userId, workYears);
    }

    /**
     * 모든 사용자의 근무년수를 계산하여 업데이트
     */
    public void updateAllUsersWorkYears() {
        log.info("전체 사용자 근무년수 업데이트 시작");
        
        List<User> allUsers = userRepository.findAll();
        int updatedCount = 0;
        
        for (User user : allUsers) {
            try {
                int workYears = CareerCalculator.calculateCareerYears(user.getJoinDate());
                user.updateWorkYears(workYears);
                userRepository.save(user);
                updatedCount++;
                log.debug("근무년수 업데이트: userId={}, workYears={}", user.getId(), workYears);
            } catch (Exception e) {
                log.error("사용자 근무년수 업데이트 실패: userId={}", user.getId(), e);
            }
        }
        
        log.info("전체 사용자 근무년수 업데이트 완료: 총 {}명 업데이트", updatedCount);
    }

    /**
     * 사용자의 현재 근무년수 조회
     */
    @Transactional(readOnly = true)
    public int getUserWorkYears(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        if (user.getWorkYears() != null) {
            return user.getWorkYears();
        }
        
        // workYears가 null인 경우 실시간 계산 후 저장
        int calculatedWorkYears = CareerCalculator.calculateCareerYears(user.getJoinDate());
        
        // 별도 트랜잭션에서 업데이트 (readOnly 트랜잭션이므로)
        updateWorkYearsAsync(userId, calculatedWorkYears);
        
        return calculatedWorkYears;
    }
    
    /**
     * 비동기적으로 workYears 업데이트 (내부 사용)
     */
    @Transactional
    public void updateWorkYearsAsync(Long userId, int workYears) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getWorkYears() == null) {
                user.updateWorkYears(workYears);
                userRepository.save(user);
                log.info("workYears 자동 설정 완료: userId={}, workYears={}", userId, workYears);
            }
        } catch (Exception e) {
            log.warn("workYears 자동 설정 실패: userId={}, workYears={}", userId, workYears, e);
        }
    }
}