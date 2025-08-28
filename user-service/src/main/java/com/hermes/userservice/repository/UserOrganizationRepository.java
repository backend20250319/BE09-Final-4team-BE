package com.hermes.userservice.repository;

import com.hermes.userservice.entity.UserOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {
    
    List<UserOrganization> findByUserId(Long userId);
    
    List<UserOrganization> findByUserIdAndIsPrimaryTrue(Long userId);
    
    List<UserOrganization> findByOrganizationId(Long organizationId);
    
    List<UserOrganization> findByOrganizationIdAndIsLeaderTrue(Long organizationId);
    
    void deleteByUserId(Long userId);
    
    @Modifying
    @Query("delete from UserOrganization uo where uo.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
