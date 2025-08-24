package com.hermes.companyinfoservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfo {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;
    
    @Column(name = "business_registration_number", nullable = false, unique = true, length = 20)
    private String businessRegistrationNumber;
    
    @Column(name = "address", nullable = false, length = 200)
    private String address;
    
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Column(name = "website", length = 200)
    private String website;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "industry", nullable = false)
    private Industry industry;
    
    @Column(name = "other_industry", length = 100)
    private String otherIndustry;// 기타 업종
    
    @Column(name = "year_established")
    private Integer yearEstablished;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_count")
    private EmployeeCount employeeCount;
    
    @Column(name = "company_introduction", columnDefinition = "TEXT")
    private String companyIntroduction;
    
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
    
    @Column(name = "logo_file_name", length = 255)
    private String logoFileName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
