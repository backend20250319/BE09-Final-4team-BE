package com.hermes.userservice.repository;

import com.hermes.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    
    // JOIN FETCH를 사용한 메서드들 (LazyInitializationException 방지)
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userOrganizations")
    List<User> findAllWithOrganizations();
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userOrganizations WHERE u.id = :userId")
    Optional<User> findByIdWithOrganizations(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userOrganizations WHERE u.email = :email")
    Optional<User> findByEmailWithOrganizations(@Param("email") String email);

    @Query("SELECT u.id FROM User u")
    List<Long> findAllUserIds();

    @Modifying
    @Query("UPDATE User u SET u.profileImageUrl = :profileImageUrl WHERE u.id = :userId")
    void updateProfileImageUrl(@Param("userId") Long userId, @Param("profileImageUrl") String profileImageUrl);
}
