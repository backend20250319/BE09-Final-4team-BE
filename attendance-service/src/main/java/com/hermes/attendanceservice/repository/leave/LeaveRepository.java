package com.hermes.attendanceservice.repository.leave;

import com.hermes.attendanceservice.entity.leave.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveRequest.RequestStatus status);
    
    @Query("SELECT l FROM LeaveRequest l WHERE l.status = :status AND l.startDate <= :endDate AND l.endDate >= :startDate")
    List<LeaveRequest> findByStatusAndDateRange(@Param("status") LeaveRequest.RequestStatus status, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
} 