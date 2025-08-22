package com.hermes.companyinfoservice.config;

import com.hermes.companyinfoservice.entity.CompanyInfo;
import com.hermes.companyinfoservice.entity.EmployeeCount;
import com.hermes.companyinfoservice.entity.Industry;
import com.hermes.companyinfoservice.repository.CompanyInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.config.uri=http://localhost:8888",
    "spring.cloud.config.name=companyinfo-service",
    "spring.cloud.config.profile=default"
})
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CompanyInfoRepository companyInfoRepository;

    @Test
    void testDatabaseConnection() throws SQLException {
        // DataSource가 정상적으로 주입되는지 확인
        assertNotNull(dataSource, "DataSource should not be null");
        
        // 데이터베이스 연결 테스트
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(5), "Database connection should be valid");
            assertFalse(connection.isClosed(), "Database connection should not be closed");
            
            System.out.println("✅ 데이터베이스 연결 확인 완료");
            System.out.println("Database URL: " + connection.getMetaData().getURL());
            System.out.println("Database Product: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("Database Version: " + connection.getMetaData().getDatabaseProductVersion());
        }
    }

    @Test
    void testJdbcTemplateConnection() {
        // JdbcTemplate이 정상적으로 작동하는지 확인
        assertNotNull(jdbcTemplate, "JdbcTemplate should not be null");
        
        // 간단한 쿼리 실행 테스트
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertEquals(1, result, "Simple query should return 1");
        
        System.out.println("✅ JdbcTemplate 연결 확인 완료");
    }

    @Test
    void testRepositoryConnection() {
        // Repository가 정상적으로 주입되는지 확인
        assertNotNull(companyInfoRepository, "CompanyInfoRepository should not be null");
        
        // 테스트 데이터 생성
        CompanyInfo testCompany = CompanyInfo.builder()
                .companyName("테스트 회사")
                .businessRegistrationNumber("999-99-99999")
                .address("테스트 주소")
                .phoneNumber("02-9999-9999")
                .email("test@test.com")
                .industry(Industry.IT_SW)
                .employeeCount(EmployeeCount.ONE_TO_TEN)
                .companyIntroduction("테스트 회사입니다.")
                .build();

        // 저장 테스트
        CompanyInfo savedCompany = companyInfoRepository.save(testCompany);
        assertNotNull(savedCompany.getId(), "Saved company should have an ID");
        
        // 조회 테스트
        Optional<CompanyInfo> foundCompany = companyInfoRepository.findById(savedCompany.getId());
        assertTrue(foundCompany.isPresent(), "Company should be found by ID");
        assertEquals("테스트 회사", foundCompany.get().getCompanyName(), "Company name should match");
        
        // 삭제 테스트
        companyInfoRepository.deleteById(savedCompany.getId());
        Optional<CompanyInfo> deletedCompany = companyInfoRepository.findById(savedCompany.getId());
        assertFalse(deletedCompany.isPresent(), "Company should be deleted");
        
        System.out.println("✅ Repository CRUD 작업 확인 완료");
    }

    @Test
    void testDatabaseSchema() {
        // 데이터베이스 스키마 확인
        String tableExistsQuery = """
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_name = 'company_info'
            )
            """;
        
        Boolean tableExists = jdbcTemplate.queryForObject(tableExistsQuery, Boolean.class);
        assertTrue(tableExists, "company_info table should exist");
        
        System.out.println("✅ 데이터베이스 스키마 확인 완료");
        System.out.println("company_info 테이블이 존재합니다.");
    }
} 