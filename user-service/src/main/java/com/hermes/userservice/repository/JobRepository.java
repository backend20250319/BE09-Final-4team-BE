package com.hermes.userservice.repository;

import com.hermes.userservice.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findById(Long id);
}
