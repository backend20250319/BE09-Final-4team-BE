package com.hermes.userservice.repository;

import com.hermes.userservice.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findById(Long id);
    List<Position> findAllByOrderBySortOrderAsc();
    boolean existsByName(String name);
}
