package com.hermes.orgservice.repository;

import com.hermes.orgservice.entity.EmployeeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAssignmentRepository extends JpaRepository<EmployeeAssignment, Long> {

    List<EmployeeAssignment> findByEmployeeId(Long employeeId);
    
    List<EmployeeAssignment> findByOrganizationId(Long organizationId);
    
    Optional<EmployeeAssignment> findByEmployeeIdAndOrganizationId(Long employeeId, Long organizationId);
    
    List<EmployeeAssignment> findByEmployeeIdAndIsPrimaryTrue(Long employeeId);
    
    List<EmployeeAssignment> findByOrganizationIdAndIsLeaderTrue(Long organizationId);
    
    @Query("SELECT ea FROM EmployeeAssignment ea WHERE ea.employeeId = :employeeId AND ea.isPrimary = true")
    List<EmployeeAssignment> findPrimaryAssignmentsByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT ea FROM EmployeeAssignment ea WHERE ea.organizationId = :organizationId AND ea.isLeader = true")
    List<EmployeeAssignment> findLeadersByOrganizationId(@Param("organizationId") Long organizationId);
    
    boolean existsByEmployeeIdAndOrganizationId(Long employeeId, Long organizationId);
    
    long countByOrganizationId(Long organizationId);
    
    long countByOrganizationIdAndIsLeaderTrue(Long organizationId);
}
