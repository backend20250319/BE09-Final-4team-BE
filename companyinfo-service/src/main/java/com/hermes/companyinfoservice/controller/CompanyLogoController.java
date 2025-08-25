package com.hermes.companyinfoservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.companyinfoservice.dto.CompanyInfoDto;
import com.hermes.companyinfoservice.service.CompanyInfoService;
// import com.hermes.ftpstarter.dto.FtpResponseDto;
// import com.hermes.ftpstarter.service.FtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/company/logo")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CompanyLogoController {

    // private final FtpService ftpService;  // FTP 서비스 주석 처리
    private final CompanyInfoService companyInfoService;

    /**
     * 회사 로고 업로드
     */
    @PostMapping("/upload/{companyId}")
    public ResponseEntity<ApiResult<CompanyInfoDto>> uploadLogo(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {
        
        log.info("Received request to upload logo for company ID: {}", companyId);
        
        // FTP 서비스가 비활성화되어 있음
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResult.failure("FTP 서비스가 현재 비활성화되어 있습니다."));
        
        /*
        try {
            // 파일 유효성 검증
            validateLogoFile(file);
            
            // 기존 회사 정보 조회
            CompanyInfoDto existingCompany = companyInfoService.getCompanyInfoById(companyId);
            
            // 기존 로고가 있다면 삭제
            if (existingCompany.getLogoFileName() != null && !existingCompany.getLogoFileName().isEmpty()) {
                try {
                    // ftpService.deleteFile(existingCompany.getLogoFileName()); // FTP 서비스 주석 처리
                    log.info("Deleted existing logo file: {}", existingCompany.getLogoFileName());
                } catch (Exception e) {
                    log.warn("Failed to delete existing logo file: {}", e.getMessage());
                }
            }
            
            // 새 로고 파일 업로드
            // FtpResponseDto ftpResponse = ftpService.uploadFile(file); // FTP 서비스 주석 처리
            
            // 회사 정보 업데이트
            // existingCompany.setLogoUrl(ftpResponse.getStoredFileName()); // 임시로 파일명을 URL로 사용 // FTP 서비스 주석 처리
            // existingCompany.setLogoFileName(ftpResponse.getStoredFileName()); // FTP 서비스 주석 처리
            
            CompanyInfoDto updatedCompany = companyInfoService.updateCompanyInfo(companyId, existingCompany);
            
            log.info("Logo uploaded successfully for company ID: {}", companyId);
            return ResponseEntity.ok(ApiResponse.success("로고가 성공적으로 업로드되었습니다.", updatedCompany));
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error while uploading logo: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Company not found with ID: {}", companyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure("해당 ID의 회사를 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("Error uploading logo for company ID {}: {}", companyId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("로고 업로드 중 오류가 발생했습니다."));
        }
        */
    }

    /**
     * 회사 로고 삭제
     */
    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResult<CompanyInfoDto>> deleteLogo(@PathVariable Long companyId) {
        log.info("Received request to delete logo for company ID: {}", companyId);
        
        // FTP 서비스가 비활성화되어 있음
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResult.failure("FTP 서비스가 현재 비활성화되어 있습니다."));
        
        /*
        try {
            // 기존 회사 정보 조회
            CompanyInfoDto existingCompany = companyInfoService.getCompanyInfoById(companyId);
            
            // 로고 파일이 있는 경우에만 삭제
            if (existingCompany.getLogoFileName() != null && !existingCompany.getLogoFileName().isEmpty()) {
                try {
                    // ftpService.deleteFile(existingCompany.getLogoFileName()); // FTP 서비스 주석 처리
                    log.info("Deleted logo file: {}", existingCompany.getLogoFileName());
                } catch (Exception e) {
                    log.warn("Failed to delete logo file: {}", e.getMessage());
                }
            }
            
            // 회사 정보에서 로고 정보 제거
            existingCompany.setLogoUrl(null);
            existingCompany.setLogoFileName(null);
            
            CompanyInfoDto updatedCompany = companyInfoService.updateCompanyInfo(companyId, existingCompany);
            
            log.info("Logo deleted successfully for company ID: {}", companyId);
            return ResponseEntity.ok(ApiResponse.success("로고가 성공적으로 삭제되었습니다.", updatedCompany));
            
        } catch (RuntimeException e) {
            log.error("Company not found with ID: {}", companyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure("해당 ID의 회사를 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("Error deleting logo for company ID {}: {}", companyId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("로고 삭제 중 오류가 발생했습니다."));
        }
        */
    }

    /**
     * 회사 로고 URL 조회
     */
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResult<String>> getLogoUrl(@PathVariable Long companyId) {
        log.info("Received request to get logo URL for company ID: {}", companyId);
        
        try {
            CompanyInfoDto company = companyInfoService.getCompanyInfoById(companyId);
            
            if (company.getLogoUrl() == null || company.getLogoUrl().isEmpty()) {
                return ResponseEntity.ok(ApiResult.success("로고가 설정되지 않았습니다.", null));
            }
            
            return ResponseEntity.ok(ApiResult.success("로고 URL을 성공적으로 조회했습니다.", company.getLogoUrl()));
            
        } catch (RuntimeException e) {
            log.error("Company not found with ID: {}", companyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResult.failure("해당 ID의 회사를 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("Error getting logo URL for company ID {}: {}", companyId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResult.failure("로고 URL 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 로고 파일 유효성 검증
     */
    private void validateLogoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일을 선택해주세요.");
        }
        
        // 파일 크기 검증 (5MB 제한)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB 이하여야 합니다.");
        }
        
        // 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!extension.matches("(jpg|jpeg|png|gif|webp)")) {
                throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
            }
        }
    }
} 