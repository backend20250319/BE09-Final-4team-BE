package com.hermes.companyinfoservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.companyinfoservice.dto.CompanyInfoDto;
import com.hermes.companyinfoservice.service.CompanyInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CompanyInfoController {
    
    private final CompanyInfoService companyInfoService;
    
    /**
     * 회사 정보 생성
     */
    @PostMapping
    public ResponseEntity<ApiResult<CompanyInfoDto>> createCompanyInfo(@RequestBody CompanyInfoDto companyInfoDto) {
        log.info("Received request to create company info: {}", companyInfoDto.getCompanyName());
        
        try {
            CompanyInfoDto createdCompanyInfo = companyInfoService.createCompanyInfo(companyInfoDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResult.success("회사 정보가 성공적으로 생성되었습니다.", createdCompanyInfo));
        } catch (IllegalArgumentException e) {
            log.error("Validation error while creating company info: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResult.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating company info: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResult.failure("회사 정보 생성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 모든 회사 정보 조회
     */
    @GetMapping
    public ResponseEntity<ApiResult<List<CompanyInfoDto>>> getAllCompanyInfos() {
        log.info("Received request to fetch all company infos");
        
        try {
            List<CompanyInfoDto> companyInfos = companyInfoService.getAllCompanyInfos();
            return ResponseEntity.ok(ApiResult.success("모든 회사 정보를 성공적으로 조회했습니다.", companyInfos));
        } catch (Exception e) {
            log.error("Error fetching all company infos: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResult.failure("회사 정보 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * ID로 회사 정보 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<CompanyInfoDto>> getCompanyInfoById(@PathVariable Long id) {
        log.info("Received request to fetch company info by ID: {}", id);
        
        try {
            CompanyInfoDto companyInfo = companyInfoService.getCompanyInfoById(id);
            return ResponseEntity.ok(ApiResult.success("회사 정보를 성공적으로 조회했습니다.", companyInfo));
        } catch (RuntimeException e) {
            log.error("Company info not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResult.failure("해당 ID의 회사 정보를 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("Error fetching company info by ID {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResult.failure("회사 정보 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 사업자등록번호로 회사 정보 조회
     */
    @GetMapping("/business-number/{businessRegistrationNumber}")
    public ResponseEntity<ApiResult<CompanyInfoDto>> getCompanyInfoByBusinessNumber(@PathVariable String businessRegistrationNumber) {
        log.info("Received request to fetch company info by business registration number: {}", businessRegistrationNumber);
        
        try {
            CompanyInfoDto companyInfo = companyInfoService.getCompanyInfoByBusinessNumber(businessRegistrationNumber);
            return ResponseEntity.ok(ApiResult.success("회사 정보를 성공적으로 조회했습니다.", companyInfo));
        } catch (RuntimeException e) {
            log.error("Company info not found with business registration number: {}", businessRegistrationNumber);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResult.failure("해당 사업자등록번호의 회사 정보를 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("Error fetching company info by business registration number {}: {}", businessRegistrationNumber, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResult.failure("회사 정보 조회 중 오류가 발생했습니다."));
        }
    }
    

    
    /**
     * 회사 정보 부분 수정 (PATCH)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResult<CompanyInfoDto>> partialUpdateCompanyInfo(@PathVariable Long id, @RequestBody CompanyInfoDto partialUpdateDto) {
        log.info("Received request to partially update company info with ID: {}", id);
        
        try {
            CompanyInfoDto updatedCompanyInfo = companyInfoService.partialUpdateCompanyInfo(id, partialUpdateDto);
            return ResponseEntity.ok(ApiResult.success("회사 정보가 성공적으로 부분 수정되었습니다.", updatedCompanyInfo));
        } catch (IllegalArgumentException e) {
            log.error("Validation error while partially updating company info: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResult.failure(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Company info not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResult.failure("해당 ID의 회사 정보를 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("Error partially updating company info with ID {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResult.failure("회사 정보 부분 수정 중 오류가 발생했습니다."));
        }
    }
    

    

} 