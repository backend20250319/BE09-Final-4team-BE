package com.hermes.attendanceservice.repository.workpolicy;

import com.hermes.attendanceservice.entity.workpolicy.WorkPolicy;
import com.hermes.attendanceservice.entity.workpolicy.WorkType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkPolicyRepository extends JpaRepository<WorkPolicy, Long> {
    
    /**
     * 이름으로 근무 정책 검색
     */
    Optional<WorkPolicy> findByName(String name);
    
    /**
     * 이름으로 근무 정책 존재 여부 확인
     */
    boolean existsByName(String name);
    
    /**
     * 근무 유형으로 근무 정책 목록 조회
     */
    List<WorkPolicy> findByType(WorkType type);
    
    /**
     * 근무 유형으로 근무 정책 목록 조회 (페이징)
     */
    Page<WorkPolicy> findByType(WorkType type, Pageable pageable);
    
    /**
     * 이름에 특정 키워드를 포함한 근무 정책 검색
     */
    @Query("SELECT wp FROM WorkPolicy wp WHERE wp.name LIKE %:keyword%")
    List<WorkPolicy> findByNameContaining(@Param("keyword") String keyword);
    
    /**
     * 이름에 특정 키워드를 포함한 근무 정책 검색(페이징)
     */
    @Query("SELECT wp FROM WorkPolicy wp WHERE wp.name LIKE %:keyword%")
    Page<WorkPolicy> findByNameContaining(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 노동법준수여부로 근무 정책 필터링
     */
    @Query("SELECT wp FROM WorkPolicy wp WHERE " +
           "CASE WHEN wp.workCycle = 'ONE_MONTH' THEN wp.totalRequiredMinutes <= 9600 " +
           "ELSE wp.totalRequiredMinutes <= 2400 END = :isCompliant")
    Page<WorkPolicy> findByLaborLawCompliance(@Param("isCompliant") boolean isCompliant, Pageable pageable);
    
    /**
     * 복합 조건으로 근무 정책 검색
     */
    @Query("SELECT wp FROM WorkPolicy wp WHERE " +
           "(:name IS NULL OR wp.name LIKE %:name%) AND " +
           "(:type IS NULL OR wp.type = :type) AND " +
           "(:isCompliant IS NULL OR " +
           "CASE WHEN wp.workCycle = 'ONE_MONTH' THEN wp.totalRequiredMinutes <= 9600 " +
           "ELSE wp.totalRequiredMinutes <= 2400 END = :isCompliant)")
    Page<WorkPolicy> findBySearchConditions(
            @Param("name") String name,
            @Param("type") WorkType type,
            @Param("isCompliant") Boolean isCompliant,
            Pageable pageable
    );
} 