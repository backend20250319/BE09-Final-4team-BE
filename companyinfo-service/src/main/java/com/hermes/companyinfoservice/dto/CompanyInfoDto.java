package com.hermes.companyinfoservice.dto;

import com.hermes.companyinfoservice.entity.CompanyInfo;
import com.hermes.companyinfoservice.entity.EmployeeCount;
import com.hermes.companyinfoservice.entity.Industry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfoDto {
    
    private Long id;
    private String companyName;
    private String businessRegistrationNumber;
    private String address;
    private String phoneNumber;
    private String email;
    private String website;
    private Industry industry;
    private String otherIndustry;
    private Integer yearEstablished;
    private EmployeeCount employeeCount;
    private String companyIntroduction;
    private String logoUrl;
    private String logoFileName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Entity를 DTO로 변환하는 정적 메서드
    public static CompanyInfoDto fromEntity(CompanyInfo entity) {
        return CompanyInfoDto.builder()
                .id(entity.getId())
                .companyName(entity.getCompanyName())
                .businessRegistrationNumber(entity.getBusinessRegistrationNumber())
                .address(entity.getAddress())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .website(entity.getWebsite())
                .industry(entity.getIndustry())
                .otherIndustry(entity.getOtherIndustry())
                .yearEstablished(entity.getYearEstablished())
                .employeeCount(entity.getEmployeeCount())
                .companyIntroduction(entity.getCompanyIntroduction())
                .logoUrl(entity.getLogoUrl())
                .logoFileName(entity.getLogoFileName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    // DTO를 Entity로 변환하는 메서드
    public CompanyInfo toEntity() {
        return CompanyInfo.builder()
                .id(this.id)
                .companyName(this.companyName)
                .businessRegistrationNumber(this.businessRegistrationNumber)
                .address(this.address)
                .phoneNumber(this.phoneNumber)
                .email(this.email)
                .website(this.website)
                .industry(this.industry)
                .otherIndustry(this.otherIndustry)
                .yearEstablished(this.yearEstablished)
                .employeeCount(this.employeeCount)
                .companyIntroduction(this.companyIntroduction)
                .logoUrl(this.logoUrl)
                .logoFileName(this.logoFileName)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
} 