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
        
        // 원격 DB에서 조직 정보 가져오기
        List<Map<String, Object>> remoteOrganizations = organizationIntegrationService.getUserOrganizations(userId);
        
        // 근무정책 정보 가져오기
        WorkPolicyResponseDto workPolicy = workPolicyIntegrationService.getWorkPolicyById(user.getWorkPolicyId());
        
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

        // DTO 필드가 null이 아닌 경우에만 업데이트 로직 적용
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
        
        // N+1 문제 해결: 모든 사용자의 조직 정보를 한 번에 가져오기
        Map<Long, List<Map<String, Object>>> allOrganizations = organizationIntegrationService.getAllUsersOrganizations();
        
        return users.stream()
                .map(user -> {
                    List<Map<String, Object>> userOrganizations = allOrganizations.getOrDefault(user.getId(), List.of());
                    
                    // 근무정책 정보 가져오기 (N+1 문제 발생 - 추후 개선 필요)
                    WorkPolicyResponseDto workPolicy = workPolicyIntegrationService.getWorkPolicyById(user.getWorkPolicyId());
                    
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
}