package com.hermes.userservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.auth.principal.UserPrincipal;
import com.hermes.userservice.dto.DetailProfileResponseDto;
import com.hermes.userservice.dto.MainProfileResponseDto;
import com.hermes.userservice.dto.UserCreateDto;
import com.hermes.userservice.dto.UserResponseDto;
import com.hermes.userservice.dto.UserUpdateDto;
import com.hermes.userservice.service.OrganizationSyncService;
import com.hermes.userservice.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrganizationSyncService organizationSyncService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResult<UserResponseDto>> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        log.info("사용자 생성 요청 (이메일): {}", userCreateDto.getEmail());
        UserResponseDto createdUserDto = userService.createUser(userCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("사용자 생성 성공", createdUserDto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResult<List<UserResponseDto>>> getAllUsers() {
        log.info("전체 사용자 목록 조회 요청");
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResult.success("사용자 목록 조회 성공", users));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public ResponseEntity<ApiResult<UserResponseDto>> getUser(@PathVariable Long userId) {
        log.info("사용자 조회 요청: userId={}", userId);
        UserResponseDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResult.success("사용자 조회 성공", userDto));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public ResponseEntity<ApiResult<UserResponseDto>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        log.info("사용자 정보 업데이트 요청: userId={}", userId);
        UserResponseDto updatedUserDto = userService.updateUser(userId, userUpdateDto);
        return ResponseEntity.ok(ApiResult.success("사용자 정보 업데이트 성공", updatedUserDto));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResult<Void>> deleteUser(@PathVariable Long userId) {
        log.info("사용자 삭제 요청: userId={}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResult.success("사용자 삭제 성공", null));
    }

    @PostMapping("/{userId}/sync-organization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResult<Void>> syncUserOrganization(@PathVariable Long userId) {
        log.info("사용자 조직 정보 동기화 요청: userId={}", userId);
        organizationSyncService.syncUserOrganizations(userId);
        return ResponseEntity.ok(ApiResult.success("사용자 조직 정보 동기화 완료", null));
    }

    @PostMapping("/sync-organizations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResult<Void>> syncAllUsersOrganizations() {
        log.info("전체 사용자 조직 정보 동기화 요청");
        organizationSyncService.syncAllUsersOrganizations();
        return ResponseEntity.ok(ApiResult.success("전체 사용자 조직 정보 동기화 완료", null));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResult<MainProfileResponseDto>> getMainProfile(@PathVariable Long userId) {
        log.info("공개 프로필 조회 요청: userId={}", userId);
        MainProfileResponseDto profile = userService.getMainProfile(userId);
        return ResponseEntity.ok(ApiResult.success("공개 프로필 조회 성공", profile));
    }

    @GetMapping("/{userId}/profile/detail")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public ResponseEntity<ApiResult<DetailProfileResponseDto>> getDetailProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("상세 프로필 조회 요청: userId={}", userId);
        DetailProfileResponseDto profile = userService.getDetailProfile(userId);
        return ResponseEntity.ok(ApiResult.success("상세 프로필 조회 성공", profile));
    }
}