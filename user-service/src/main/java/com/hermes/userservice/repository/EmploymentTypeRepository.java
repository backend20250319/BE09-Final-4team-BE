package com.hermes.userservice.repository;

import com.hermes.userservice.entity.EmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmploymentTypeRepository extends JpaRepository<EmploymentType, Long> {
    Optional<EmploymentType> findById(Long id);
}
