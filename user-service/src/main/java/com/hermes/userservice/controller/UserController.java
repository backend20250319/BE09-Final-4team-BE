package com.hermes.userservice.controller;

import com.hermes.auth.dto.ApiResponse;
import com.hermes.userservice.dto.UserCreateDto;
import com.hermes.userservice.dto.UserDetailResponseDto;
import com.hermes.userservice.dto.UserResponseDto;
import com.hermes.userservice.dto.UserUpdateDto;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.mapper.UserMapper;
import com.hermes.userservice.service.UserService;
import com.hermes.userservice.service.WorkPolicyIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WorkPolicyIntegrationService workPolicyIntegrationService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        log.info("사용자 생성 요청 (이메일): {}", userCreateDto.getEmail());

        User createdUser = userService.createUser(userCreateDto);
        UserResponseDto userResponseDto = userMapper.toResponseDto(createdUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("사용자 생성 성공", userResponseDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        log.info("전체 사용자 목록 조회 요청");

        List<User> users = userService.getAllUsers();
        List<UserResponseDto> userResponseDtos = users.stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회 성공", userResponseDtos));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponseDto>> getUser(@PathVariable Long userId) {
        log.info("사용자 조회 요청: userId={}", userId);

        User user = userService.getUserById(userId);
        var workPolicy = workPolicyIntegrationService.getUserWorkPolicy(userId);

        UserResponseDto userResponseDto = userMapper.toResponseDto(user);

        UserDetailResponseDto result = UserDetailResponseDto.builder()
                .user(userResponseDto)
                .workPolicy(workPolicy)
                .build();

        return ResponseEntity.ok(ApiResponse.success("사용자 조회 성공", result));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> patchUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateDto userUpdateDto) {

        log.info("사용자 정보 부분 업데이트 요청: userId={}", userId);

        User updatedUser = userService.updateUser(userId, userUpdateDto);
        UserResponseDto userResponseDto = userMapper.toResponseDto(updatedUser);

        return ResponseEntity.ok(ApiResponse.success("사용자 정보 업데이트 성공", userResponseDto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        log.info("사용자 삭제 요청: userId={}", userId);

        userService.deleteUser(userId);

        return ResponseEntity.ok(ApiResponse.success("사용자 삭제 성공", null));
    }
}