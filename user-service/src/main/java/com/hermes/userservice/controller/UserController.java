package com.hermes.userservice.controller;

import com.hermes.auth.dto.ApiResponse;
import com.hermes.userservice.dto.UserResponseDto;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.mapper.UserMapper;
import com.hermes.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final UserMapper userMapper;
    
    /**
     * 사용자 ID로 사용자 정보 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long userId) {
        try {
            log.info("Get user by id: {}", userId);
            User user = userService.getUserById(userId);
            UserResponseDto userResponse = userMapper.toResponseDto(user);
            return ResponseEntity.ok(ApiResponse.success("사용자 정보를 성공적으로 조회했습니다.", userResponse));
        } catch (Exception e) {
            log.error("Error getting user by id: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보 조회에 실패했습니다.", "USER_NOT_FOUND"));
        }
    }
}