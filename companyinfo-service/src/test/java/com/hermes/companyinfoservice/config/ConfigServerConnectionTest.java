package com.hermes.companyinfoservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.config.uri=http://localhost:8888",
    "spring.cloud.config.name=companyinfo-service",
    "spring.cloud.config.profile=default"
})
public class ConfigServerConnectionTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ConfigClientProperties configClientProperties;

    @Test
    void testConfigServerConnection() {
        // Config Server 연결 설정 확인
        assertNotNull(configClientProperties, "ConfigClientProperties should not be null");
        assertEquals("http://localhost:8888", configClientProperties.getUri()[0], "Config Server URI should match");
        assertEquals("companyinfo-service", configClientProperties.getName(), "Service name should match");
        assertEquals("default", configClientProperties.getProfile(), "Profile should match");
        
        System.out.println("✅ Config Server 연결 설정 확인 완료");
        System.out.println("URI: " + configClientProperties.getUri()[0]);
        System.out.println("Service Name: " + configClientProperties.getName());
        System.out.println("Profile: " + configClientProperties.getProfile());
    }

    @Test
    void testApplicationContextLoads() {
        // 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인
        assertNotNull(applicationContext, "ApplicationContext should not be null");
        
        // Bean이 정상적으로 등록되었는지 확인
        assertTrue(applicationContext.containsBean("companyInfoService"), "CompanyInfoService bean should be registered");
        
        System.out.println("✅ 애플리케이션 컨텍스트 로드 확인 완료");
    }
} 