package com.hermes.userservice.controller;

import com.hermes.auth.dto.ApiResponse;
import com.hermes.userservice.dto.UserCreateDto;
import com.hermes.userservice.dto.UserResponseDto;
import com.hermes.userservice.dto.UserUpdateDto;
import com.hermes.userservice.service.OrganizationSyncService;
import com.hermes.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrganizationSyncService organizationSyncService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        log.info("사용자 생성 요청 (이메일): {}", userCreateDto.getEmail());
        UserResponseDto createdUserDto = userService.createUser(userCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("사용자 생성 성공", createdUserDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        log.info("전체 사용자 목록 조회 요청");
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회 성공", users));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@PathVariable Long userId) {
        log.info("사용자 조회 요청: userId={}", userId);
        UserResponseDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("사용자 조회 성공", userDto));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        log.info("사용자 정보 업데이트 요청: userId={}", userId);
        UserResponseDto updatedUserDto = userService.updateUser(userId, userUpdateDto);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 업데이트 성공", updatedUserDto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        log.info("사용자 삭제 요청: userId={}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("사용자 삭제 성공", null));
    }

    // 조직 동기화는 별도의 엔드포인트로 분리
    @PostMapping("/{userId}/sync-organization")
    public ResponseEntity<ApiResponse<Void>> syncUserOrganization(@PathVariable Long userId) {
        log.info("사용자 조직 정보 동기화 요청: userId={}", userId);
        organizationSyncService.syncUserOrganizations(userId);
        return ResponseEntity.ok(ApiResponse.success("사용자 조직 정보 동기화 완료", null));
    }

    @PostMapping("/sync-organizations")
    public ResponseEntity<ApiResponse<Void>> syncAllUsersOrganizations() {
        log.info("전체 사용자 조직 정보 동기화 요청");
        organizationSyncService.syncAllUsersOrganizations();
        return ResponseEntity.ok(ApiResponse.success("전체 사용자 조직 정보 동기화 완료", null));
    }
}