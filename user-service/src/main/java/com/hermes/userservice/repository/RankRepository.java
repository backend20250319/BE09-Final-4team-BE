package com.hermes.userservice.repository;

import com.hermes.userservice.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RankRepository extends JpaRepository<Rank, Long> {
    Optional<Rank> findById(Long id);
    List<Rank> findAllByOrderBySortOrderAsc();
    boolean existsByName(String name);
}
