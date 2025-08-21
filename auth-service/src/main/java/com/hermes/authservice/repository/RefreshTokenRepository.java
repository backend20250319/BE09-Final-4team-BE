package com.hermes.authservice.repository;

import com.hermes.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    // 만료된 토큰 조회
    List<RefreshToken> findByExpirationBefore(LocalDateTime expiration);
}