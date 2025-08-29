package com.hermes.userservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 완전한 프로필 정보를 포함하는 DTO
 * 공개 정보 + 민감한 정보를 모두 포함
 * 본인 및 관리자만 접근 가능
 */
@Getter
@Builder
public class DetailProfileResponseDto {
    
    // === 공개 정보 (MainProfile과 동일) ===
    /**
     * 사용자 ID
     */
    private Long id;
    
    /**
     * 사용자 이름
     */
    private String name;
    
    /**
     * 이메일 주소
     */
    private String email;
    
    /**
     * 전화번호
     */
    private String phone;
    
    /**
     * 프로필 이미지 URL
     */
    private String profileImageUrl;
    
    // === 민감한 상세 정보 ===
    /**
     * 사용자 주소
     */
    private String address;
    
    /**
     * 가입일
     */
    private LocalDate joinDate;
}
