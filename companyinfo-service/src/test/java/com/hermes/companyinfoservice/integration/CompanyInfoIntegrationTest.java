package com.hermes.companyinfoservice.integration;

import com.hermes.api.common.ApiResult;
import com.hermes.companyinfoservice.dto.CompanyInfoDto;
import com.hermes.companyinfoservice.entity.EmployeeCount;
import com.hermes.companyinfoservice.entity.Industry;
import com.hermes.companyinfoservice.service.CompanyInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.cloud.config.uri=http://localhost:8888",
    "spring.cloud.config.name=companyinfo-service",
    "spring.cloud.config.profile=default"
})
public class CompanyInfoIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompanyInfoService companyInfoService;

    @Test
    void testHealthCheckEndpoint() {
        // 헬스 체크 엔드포인트 테스트
        String url = "http://localhost:" + port + "/api/company/health";
        ResponseEntity<ApiResult> response = restTemplate.getForEntity(url, ApiResult.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getStatus());
        
        System.out.println("✅ 헬스 체크 엔드포인트 확인 완료");
    }

    @Test
    void testCreateCompanyInfoEndpoint() {
        // 회사 정보 생성 API 테스트
        String url = "http://localhost:" + port + "/api/company";
        
        CompanyInfoDto companyInfoDto = CompanyInfoDto.builder()
                .companyName("통합테스트 회사")
                .businessRegistrationNumber("888-88-88888")
                .address("통합테스트 주소")
                .phoneNumber("02-8888-8888")
                .email("integration@test.com")
                .website("https://integration-test.com")
                .industry(Industry.IT_SW)
                .yearEstablished(2024)
                .employeeCount(EmployeeCount.FIFTY_TO_HUNDRED)
                .companyIntroduction("통합 테스트용 회사입니다.")
                .build();

        ResponseEntity<ApiResult> response = restTemplate.postForEntity(url, companyInfoDto, ApiResult.class);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getStatus());
        assertEquals("회사 정보가 성공적으로 생성되었습니다.", response.getBody().getMessage());
        
        System.out.println("✅ 회사 정보 생성 API 확인 완료");
    }

    @Test
    void testGetAllCompanyInfosEndpoint() {
        // 전체 회사 정보 조회 API 테스트
        String url = "http://localhost:" + port + "/api/company";
        ResponseEntity<ApiResult> response = restTemplate.getForEntity(url, ApiResult.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getStatus());
        
        System.out.println("✅ 전체 회사 정보 조회 API 확인 완료");
    }

    @Test
    void testServiceLayerIntegration() {
        // 서비스 레이어 통합 테스트
        CompanyInfoDto companyInfoDto = CompanyInfoDto.builder()
                .companyName("서비스테스트 회사")
                .businessRegistrationNumber("777-77-77777")
                .address("서비스테스트 주소")
                .phoneNumber("02-7777-7777")
                .email("service@test.com")
                .industry(Industry.MANUFACTURING)
                .employeeCount(EmployeeCount.HUNDRED_TO_FIVE_HUNDRED)
                .companyIntroduction("서비스 테스트용 회사입니다.")
                .build();

        // 생성 테스트
        CompanyInfoDto createdCompany = companyInfoService.createCompanyInfo(companyInfoDto);
        assertNotNull(createdCompany.getId());
        assertEquals("서비스테스트 회사", createdCompany.getCompanyName());

        // 조회 테스트
        CompanyInfoDto foundCompany = companyInfoService.getCompanyInfoById(createdCompany.getId());
        assertEquals(createdCompany.getId(), foundCompany.getId());

    }

    @Test
    void testConfigServerIntegration() {
        // Config Server에서 설정을 가져오는지 확인
        assertNotNull(companyInfoService, "CompanyInfoService should be injected");
        
        // 애플리케이션이 정상적으로 시작되었는지 확인
        String url = "http://localhost:" + port + "/api/company/health";
        ResponseEntity<ApiResult> response = restTemplate.getForEntity(url, ApiResult.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        System.out.println("✅ Config Server 통합 테스트 완료");
        System.out.println("서버 포트: " + port);
    }
} 