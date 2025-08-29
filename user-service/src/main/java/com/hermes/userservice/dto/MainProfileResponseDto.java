package com.hermes.userservice.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 공개 가능한 최소한의 프로필 정보를 포함하는 DTO
 * 모든 사용자가 볼 수 있는 기본적인 정보만 포함
 */
@Getter
@Builder
public class MainProfileResponseDto {
    
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
}
