package com.hermes.userservice.repository;

import com.hermes.userservice.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findById(Long id);
}
