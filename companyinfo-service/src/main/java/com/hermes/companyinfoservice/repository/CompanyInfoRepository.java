package com.hermes.companyinfoservice.repository;

import com.hermes.companyinfoservice.entity.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {
    
    // 사업자등록번호로 회사 정보 조회
    Optional<CompanyInfo> findByBusinessRegistrationNumber(String businessRegistrationNumber);
    
    // 회사명으로 회사 정보 조회
    Optional<CompanyInfo> findByCompanyName(String companyName);
    
    // 사업자등록번호 존재 여부 확인
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);
    
    // 회사명 존재 여부 확인
    boolean existsByCompanyName(String companyName);
} 