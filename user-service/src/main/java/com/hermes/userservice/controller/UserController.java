package com.hermes.userservice.controller;

import com.hermes.auth.dto.ApiResponse;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.service.UserService;
import com.hermes.userservice.service.WorkPolicyIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WorkPolicyIntegrationService workPolicyIntegrationService;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody Map<String, Object> request) {
        log.info("사용자 생성 요청: {}", request);
        
        try {
            String name = (String) request.get("name");
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            Long workPolicyId = request.get("workPolicyId") != null ? 
                    Long.valueOf(request.get("workPolicyId").toString()) : null;
            
            User createdUser = userService.createUser(name, email, password, workPolicyId);
            
            return ResponseEntity.ok(ApiResponse.success("사용자 생성 성공", createdUser));
            
        } catch (Exception e) {
            log.error("사용자 생성 실패: error={}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 생성 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUser(@PathVariable Long userId) {
        log.info("사용자 조회 요청: userId={}", userId);
        
        try {
            User user = userService.getUserById(userId);
            Map<String, Object> workPolicy = workPolicyIntegrationService.getUserWorkPolicy(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("workPolicy", workPolicy);
            
            return ResponseEntity.ok(ApiResponse.success("사용자 조회 성공", result));
            
        } catch (Exception e) {
            log.error("사용자 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 조회 실패: " + e.getMessage()));
        }
    }

    @PutMapping("/{userId}/work-policy")
    public ResponseEntity<ApiResponse<User>> updateUserWorkPolicy(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {
        
        log.info("사용자 근무정책 업데이트 요청: userId={}, workPolicyId={}", 
                userId, request.get("workPolicyId"));
        
        try {
            Long workPolicyId = Long.valueOf(request.get("workPolicyId").toString());
            User updatedUser = userService.updateUserWorkPolicy(userId, workPolicyId);
            
            return ResponseEntity.ok(ApiResponse.success("근무정책 업데이트 성공", updatedUser));
            
        } catch (Exception e) {
            log.error("근무정책 업데이트 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("근무정책 업데이트 실패: " + e.getMessage()));
        }
    }

    @PatchMapping("/{userId}/work-policy")
    public ResponseEntity<ApiResponse<User>> patchUserWorkPolicy(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {
        
        log.info("사용자 근무정책 부분 업데이트 요청: userId={}, workPolicyId={}", 
                userId, request.get("workPolicyId"));
        
        try {
            Long workPolicyId = Long.valueOf(request.get("workPolicyId").toString());
            User updatedUser = userService.updateUserWorkPolicy(userId, workPolicyId);
            
            return ResponseEntity.ok(ApiResponse.success("근무정책 부분 업데이트 성공", updatedUser));
            
        } catch (Exception e) {
            log.error("근무정책 부분 업데이트 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("근무정책 부분 업데이트 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/work-policy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserWorkPolicy(@PathVariable Long userId) {
        log.info("사용자 근무정책 조회 요청: userId={}", userId);
        
        try {
            Map<String, Object> workPolicy = workPolicyIntegrationService.getUserWorkPolicy(userId);
            return ResponseEntity.ok(ApiResponse.success("근무정책 조회 성공", workPolicy));
            
        } catch (Exception e) {
            log.error("근무정책 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("근무정책 조회 실패: " + e.getMessage()));
        }
    }
}
