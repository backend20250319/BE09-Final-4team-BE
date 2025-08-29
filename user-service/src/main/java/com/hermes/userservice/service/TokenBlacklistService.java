package com.hermes.userservice.service;

/**
 * 토큰 블랙리스트 관리 서비스 인터페이스
 * 로그아웃된 토큰이나 무효화된 토큰을 관리하여 보안을 강화합니다.
 */
public interface TokenBlacklistService {

    /**
     * 개별 토큰을 블랙리스트에 추가
     * 
     * @param token 블랙리스트에 추가할 토큰
     * @param duration 블랙리스트에 유지할 시간 (초)
     * @param userId 토큰의 소유자 ID
     */
    void addToken(String token, long duration, Long userId);


    /**
     * 토큰이 블랙리스트에 있는지 확인
     * 
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    boolean isBlacklisted(String token);

    /**
     * 특정 토큰을 블랙리스트에서 제거
     * 
     * @param token 제거할 토큰
     * @return 제거되었으면 true, 토큰이 없었으면 false
     */
    boolean removeToken(String token);

    /**
     * 만료된 토큰들을 블랙리스트에서 자동 제거
     * 메모리 효율성을 위해 정기적으로 호출되어야 합니다.
     * 
     * @return 제거된 토큰 개수
     */
    int removeExpiredTokens();

    /**
     * 현재 블랙리스트에 있는 토큰 개수 반환
     * 
     * @return 블랙리스트 크기
     */
    int getSize();

    /**
     * 블랙리스트 전체 초기화 (테스트 및 관리 목적)
     * 운영 환경에서는 신중히 사용해야 합니다.
     */
    void clear();

    /**
     * 특정 사용자의 모든 토큰을 블랙리스트에서 제거
     * 
     * @param userId 사용자 ID
     * @return 제거된 토큰 개수
     */
    int removeUserTokens(Long userId);
}