package com.hermes.gatewayserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.web-application-type=reactive"
})
class GatewayServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
