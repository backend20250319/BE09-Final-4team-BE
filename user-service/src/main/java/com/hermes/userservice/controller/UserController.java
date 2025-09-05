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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

import com.hermes.userservice.dto.ColleagueSearchRequestDto;
import com.hermes.userservice.dto.ColleagueResponseDto;

@Slf4j
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Tag(name = "사용자 관리 API", description = "사용자 정보 조회, 생성, 수정, 삭제 및 조직 동기화 기능 제공")
public class UserController {

    private final UserService userService;
    private final OrganizationSyncService organizationSyncService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 시스템에 등록합니다. 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "사용자 생성 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
            @ApiResponse(responseCode = "409", description = "중복된 이메일")
    })
    public ResponseEntity<ApiResult<UserResponseDto>> createUser(
            @Parameter(description = "생성할 사용자 정보", required = true)
            @Valid @RequestBody UserCreateDto userCreateDto) {
        log.info("사용자 생성 요청 (이메일): {}", userCreateDto.getEmail());
        UserResponseDto createdUserDto = userService.createUser(userCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("사용자 생성 성공", createdUserDto));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // ADMIN 권한 제거, 인증된 사용자면 접근 가능
    @Operation(summary = "전체 사용자 목록 조회", description = "시스템에 등록된 모든 사용자의 정보를 조회합니다. 인증된 사용자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)")
    })
    public ResponseEntity<ApiResult<List<UserResponseDto>>> getAllUsers() {
        log.info("전체 사용자 목록 조회 요청");
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResult.success("사용자 목록 조회 성공", users));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사용자 정보 조회", description = "특정 사용자의 정보를 조회합니다. 인증된 사용자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<UserResponseDto>> getUser(
            @Parameter(description = "조회할 사용자 ID", required = true, example = "1")
            @PathVariable Long userId) {
        log.info("사용자 조회 요청: userId={}", userId);
        UserResponseDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResult.success("사용자 조회 성공", userDto));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 정보 수정", description = "사용자의 정보를 수정합니다. 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "중복된 이메일")
    })
    public ResponseEntity<ApiResult<UserResponseDto>> updateUser(
            @Parameter(description = "수정할 사용자 ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "수정할 사용자 정보", required = true)
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        log.info("사용자 정보 업데이트 요청: userId={}", userId);
        UserResponseDto updatedUserDto = userService.updateUser(userId, userUpdateDto);
        return ResponseEntity.ok(ApiResult.success("사용자 정보 업데이트 성공", updatedUserDto));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 삭제", description = "사용자를 시스템에서 삭제합니다. 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<Void>> deleteUser(
            @Parameter(description = "삭제할 사용자 ID", required = true, example = "1")
            @PathVariable Long userId) {
        log.info("사용자 삭제 요청: userId={}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResult.success("사용자 삭제 성공", null));
    }

    @PostMapping("/{userId}/sync-organization")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 조직 정보 동기화", description = "특정 사용자의 조직 정보를 외부 시스템과 동기화합니다. 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조직 정보 동기화 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<Void>> syncUserOrganization(
            @Parameter(description = "동기화할 사용자 ID", required = true, example = "1")
            @PathVariable Long userId) {
        log.info("사용자 조직 정보 동기화 요청: userId={}", userId);
        organizationSyncService.syncUserOrganizations(userId);
        return ResponseEntity.ok(ApiResult.success("사용자 조직 정보 동기화 완료", null));
    }

    @PostMapping("/sync-organizations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 사용자 조직 정보 동기화", description = "모든 사용자의 조직 정보를 외부 시스템과 동기화합니다. 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 조직 정보 동기화 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)")
    })
    public ResponseEntity<ApiResult<Void>> syncAllUsersOrganizations() {
        log.info("전체 사용자 조직 정보 동기화 요청");
        organizationSyncService.syncAllUsersOrganizations();
        return ResponseEntity.ok(ApiResult.success("전체 사용자 조직 정보 동기화 완료", null));
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "공개 프로필 조회", description = "사용자의 공개 프로필 정보를 조회합니다. 인증 없이 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = MainProfileResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<MainProfileResponseDto>> getMainProfile(
            @Parameter(description = "조회할 사용자 ID", required = true, example = "1")
            @PathVariable Long userId) {
        log.info("공개 프로필 조회 요청: userId={}", userId);
        MainProfileResponseDto profile = userService.getMainProfile(userId);
        return ResponseEntity.ok(ApiResult.success("공개 프로필 조회 성공", profile));
    }

    @GetMapping("/{userId}/profile/detail")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    @Operation(summary = "상세 프로필 조회", description = "사용자의 상세 프로필 정보를 조회합니다. 본인 또는 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상세 프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = DetailProfileResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<DetailProfileResponseDto>> getDetailProfile(
            @Parameter(description = "조회할 사용자 ID", required = true, example = "1")
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("상세 프로필 조회 요청: userId={}", userId);
        DetailProfileResponseDto profile = userService.getDetailProfile(userId);
        return ResponseEntity.ok(ApiResult.success("상세 프로필 조회 성공", profile));
    }

    @GetMapping("/colleagues")
    @Operation(summary = "동료 목록 조회", description = "검색 조건에 따른 동료 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동료 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResult<List<ColleagueResponseDto>>> getColleagues(
            @ModelAttribute ColleagueSearchRequestDto searchRequest) {
        log.info("동료 목록 조회 요청: searchKeyword={}, department={}, position={}",
                searchRequest.getSearchKeyword(), searchRequest.getDepartment(), searchRequest.getPosition());
        List<ColleagueResponseDto> colleagues = userService.getColleagues(searchRequest);
        return ResponseEntity.ok(ApiResult.success("동료 목록 조회 성공", colleagues));
    }

    @GetMapping("/count")
    @Operation(summary = "전체 직원 수 조회", description = "시스템에 등록된 전체 직원 수를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 직원 수 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족")
    })
    public ResponseEntity<ApiResult<Map<String, Object>>> getTotalUsers(
            @RequestHeader("Authorization") String authorization) {
        log.info("전체 직원 수 조회 요청");
        long totalUsers = userService.getTotalEmployees();
        Map<String, Object> response = Map.of("totalUsers", totalUsers);
        return ResponseEntity.ok(ApiResult.success("전체 직원 수 조회 성공", response));
    }

    @GetMapping("/{userId}/simple")
    @Operation(summary = "간단한 사용자 정보 조회", description = "attendance-service에서 사용하는 간단한 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<Map<String, Object>> getUserSimple(@PathVariable Long userId) {
        log.info("간단한 사용자 정보 조회 요청: userId={}", userId);
        try {
            UserResponseDto userDto = userService.getUserById(userId);
            Map<String, Object> simpleUser = new HashMap<>();
            simpleUser.put("id", userDto.getId());
            simpleUser.put("workPolicyId", userDto.getWorkPolicyId());
            return ResponseEntity.ok(simpleUser);
        } catch (Exception e) {
            log.error("간단한 사용자 정보 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ids")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 사용자 ID 목록 조회", description = "알림 발송을 위한 전체 사용자 ID 목록을 조회합니다. ADMIN 권한 필요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 ID 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)")
    })
    public ResponseEntity<ApiResult<List<Long>>> getAllUserIds() {
        log.info("전체 사용자 ID 목록 조회 요청 (알림 발송용)");
        List<Long> userIds = userService.getAllUserIds();
        return ResponseEntity.ok(ApiResult.success("사용자 ID 목록 조회 성공", userIds));
    }
}