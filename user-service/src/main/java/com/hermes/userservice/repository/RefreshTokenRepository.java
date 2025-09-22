package com.hermes.userservice.repository;

import com.hermes.userservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserId(Long userId);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiration < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiration < :now")
    void deleteExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId AND rt.expiration < :now")
    void deleteExpiredTokensByUserId(@Param("userId") Long userId, @Param("now") Instant now);
}