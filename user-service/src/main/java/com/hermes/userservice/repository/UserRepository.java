package com.hermes.userservice.repository;

import com.hermes.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByNameContaining(String name);
    List<User> findByEmailContaining(String email);
    List<User> findByIsAdmin(Boolean isAdmin);

    Page<User> findAll(Pageable pageable);
    Page<User> findByNameContainingOrEmailContaining(String name, String email, Pageable pageable);

    Page<User> findByIsAdmin(Boolean isAdmin, Pageable pageable);
}
