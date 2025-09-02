package com.hermes.userservice.service;

import com.hermes.userservice.dto.UserCreateDto;
import com.hermes.userservice.dto.UserResponseDto;
import com.hermes.userservice.dto.UserUpdateDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.DuplicateEmailException;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.mapper.UserMapper;
import com.hermes.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.hermes.userservice.dto.MainProfileResponseDto;
import com.hermes.userservice.dto.DetailProfileResponseDto;
import com.hermes.userservice.dto.ColleagueResponseDto;
import com.hermes.userservice.dto.ColleagueSearchRequestDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final OrganizationIntegrationService organizationIntegrationService;
    private final WorkPolicyIntegrationService workPolicyIntegrationService;

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
        if (userRepository.findByEmail(userCreateDto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + userCreateDto.getEmail());
        }

        User user = userMapper.toEntity(userCreateDto);
        User createdUser = userRepository.save(user);
        return userMapper.toResponseDto(createdUser);
    }

    public UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        if (userUpdateDto.getEmail() != null && !Objects.equals(user.getEmail(), userUpdateDto.getEmail())) {
            if (userRepository.findByEmail(userUpdateDto.getEmail()).isPresent()) {
                throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + userUpdateDto.getEmail());
            }
            user.updateEmail(userUpdateDto.getEmail());
        }
        if (userUpdateDto.getPassword() != null) {
            user.updatePassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }
        user.updateInfo(userUpdateDto.getName(), userUpdateDto.getPhone(), userUpdateDto.getAddress(), userUpdateDto.getProfileImageUrl(), userUpdateDto.getSelfIntroduction());
        user.updateWorkInfo(userUpdateDto.getEmploymentType(), userUpdateDto.getRank(), userUpdateDto.getPosition(), userUpdateDto.getJob(), userUpdateDto.getRole(), userUpdateDto.getWorkPolicyId());
        user.updateAdminStatus(userUpdateDto.getIsAdmin());
        user.updatePasswordResetFlag(userUpdateDto.getNeedsPasswordReset());

        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDto(updatedUser);
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
        
        return users.stream()
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
        
        return userMapper.toMainProfileDto(user);
    }

    @Transactional(readOnly = true)
    public DetailProfileResponseDto getDetailProfile(Long userId) {
        log.info("상세 프로필 조회 요청: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        return userMapper.toDetailProfileDto(user);
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
                        .position(user.getPosition() != null ? user.getPosition().getName() : null)
                        .department("")
                        .avatar(user.getProfileImageUrl())
                        .employeeNumber("")
                        .status("ACTIVE")
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getTotalEmployees() {
        log.info("전체 직원 수 조회");
        return userRepository.count();
    }
}