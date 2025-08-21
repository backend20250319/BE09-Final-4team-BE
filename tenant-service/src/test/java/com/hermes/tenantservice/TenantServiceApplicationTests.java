package com.hermes.tenantservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TenantServiceApplicationTests {

    @Test
    void contextLoads() {
        // Spring 컨텍스트가 정상적으로 로드되는지 확인
    }
}
