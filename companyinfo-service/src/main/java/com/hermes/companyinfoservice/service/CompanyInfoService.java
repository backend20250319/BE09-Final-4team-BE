package com.hermes.companyinfoservice.service;

import com.hermes.companyinfoservice.dto.CompanyInfoDto;
import com.hermes.companyinfoservice.entity.CompanyInfo;
import com.hermes.companyinfoservice.entity.Industry;
import com.hermes.companyinfoservice.repository.CompanyInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanyInfoService {
    
    private final CompanyInfoRepository companyInfoRepository;
    
    /**
     * 회사 정보 생성
     */
    public CompanyInfoDto createCompanyInfo(CompanyInfoDto companyInfoDto) {
        log.info("Creating company info: {}", companyInfoDto.getCompanyName());
        
        // 비즈니스 로직 검증
        validateCompanyInfo(companyInfoDto);
        
        // DTO를 Entity로 변환
        CompanyInfo companyInfo = companyInfoDto.toEntity();
        
        // 저장
        CompanyInfo savedCompanyInfo = companyInfoRepository.save(companyInfo);
        
        log.info("Company info created successfully with ID: {}", savedCompanyInfo.getId());
        return CompanyInfoDto.fromEntity(savedCompanyInfo);
    }
    
    /**
     * 모든 회사 정보 조회
     */
    @Transactional(readOnly = true)
    public List<CompanyInfoDto> getAllCompanyInfos() {
        log.info("Fetching all company infos");
        
        List<CompanyInfo> companyInfos = companyInfoRepository.findAll();
        return companyInfos.stream()
                .map(CompanyInfoDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * ID로 회사 정보 조회
     */
    @Transactional(readOnly = true)
    public CompanyInfoDto getCompanyInfoById(Long id) {
        log.info("Fetching company info by ID: {}", id);
        
        CompanyInfo companyInfo = companyInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company info not found with ID: " + id));
        
        return CompanyInfoDto.fromEntity(companyInfo);
    }
    
    /**
     * 사업자등록번호로 회사 정보 조회
     */
    @Transactional(readOnly = true)
    public CompanyInfoDto getCompanyInfoByBusinessNumber(String businessRegistrationNumber) {
        log.info("Fetching company info by business registration number: {}", businessRegistrationNumber);
        
        CompanyInfo companyInfo = companyInfoRepository.findByBusinessRegistrationNumber(businessRegistrationNumber)
                .orElseThrow(() -> new RuntimeException("Company info not found with business registration number: " + businessRegistrationNumber));
        
        return CompanyInfoDto.fromEntity(companyInfo);
    }
    

    
    /**
     * 회사 정보 부분 수정
     */
    public CompanyInfoDto partialUpdateCompanyInfo(Long id, CompanyInfoDto partialUpdateDto) {
        log.info("Partially updating company info with ID: {}", id);
        
        // 기존 회사 정보 조회
        CompanyInfo existingCompany = companyInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company info not found with ID: " + id));
        
        // 부분 수정 로직
        if (partialUpdateDto.getCompanyName() != null) {
            existingCompany.setCompanyName(partialUpdateDto.getCompanyName());
        }
        if (partialUpdateDto.getAddress() != null) {
            existingCompany.setAddress(partialUpdateDto.getAddress());
        }
        if (partialUpdateDto.getPhoneNumber() != null) {
            existingCompany.setPhoneNumber(partialUpdateDto.getPhoneNumber());
        }
        if (partialUpdateDto.getEmail() != null) {
            existingCompany.setEmail(partialUpdateDto.getEmail());
        }
        if (partialUpdateDto.getWebsite() != null) {
            existingCompany.setWebsite(partialUpdateDto.getWebsite());
        }
        if (partialUpdateDto.getIndustry() != null) {
            existingCompany.setIndustry(partialUpdateDto.getIndustry());
            // 기타 업종 처리
            if (partialUpdateDto.getIndustry() == Industry.OTHER) {
                existingCompany.setOtherIndustry(partialUpdateDto.getOtherIndustry());
            } else {
                existingCompany.setOtherIndustry(null);
            }
        }
        if (partialUpdateDto.getYearEstablished() != null) {
            existingCompany.setYearEstablished(partialUpdateDto.getYearEstablished());
        }
        if (partialUpdateDto.getEmployeeCount() != null) {
            existingCompany.setEmployeeCount(partialUpdateDto.getEmployeeCount());
        }
        if (partialUpdateDto.getCompanyIntroduction() != null) {
            existingCompany.setCompanyIntroduction(partialUpdateDto.getCompanyIntroduction());
        }
        
        // 저장
        CompanyInfo savedCompany = companyInfoRepository.save(existingCompany);
        
        log.info("Company info partially updated successfully with ID: {}", savedCompany.getId());
        return CompanyInfoDto.fromEntity(savedCompany);
    }
    
    /**
     * 회사 정보 검증
     */
    private void validateCompanyInfo(CompanyInfoDto companyInfoDto) {
        // 필수 필드 검증
        if (companyInfoDto.getCompanyName() == null || companyInfoDto.getCompanyName().trim().isEmpty()) {
            throw new IllegalArgumentException("회사명은 필수입니다.");
        }
        
        if (companyInfoDto.getBusinessRegistrationNumber() == null || companyInfoDto.getBusinessRegistrationNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("사업자등록번호는 필수입니다.");
        }
        
        if (companyInfoDto.getAddress() == null || companyInfoDto.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("주소는 필수입니다.");
        }
        
        if (companyInfoDto.getPhoneNumber() == null || companyInfoDto.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        
        if (companyInfoDto.getEmail() == null || companyInfoDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        
        if (companyInfoDto.getIndustry() == null) {
            throw new IllegalArgumentException("업종은 필수입니다.");
        }
        
        // 기타 업종 선택 시 업종명 입력 검증
        if (companyInfoDto.getIndustry() == Industry.OTHER && 
            (companyInfoDto.getOtherIndustry() == null || companyInfoDto.getOtherIndustry().trim().isEmpty())) {
            throw new IllegalArgumentException("기타 업종 선택 시 업종명을 입력해주세요.");
        }
        
        // 사업자등록번호 중복 검증 (새로 생성하는 경우)
        if (companyInfoDto.getId() == null && 
            companyInfoRepository.existsByBusinessRegistrationNumber(companyInfoDto.getBusinessRegistrationNumber())) {
            throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다.");
        }
    }
} 