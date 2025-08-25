package com.hermes.gatewayserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Gateway OpenAPI 통합 설정
 */
@Configuration
public class GatewayOpenApiConfig {

    @Value("${server.port:9000}")
    private String serverPort;

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hermes API Gateway")
                        .description("모든 마이크로서비스의 통합 API 문서")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hermes Development Team")
                                .email("dev@hermes.com")
                                .url("https://hermes.com"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Gateway Server (Local)")
                ));
    }

    @Bean
    public List<GroupedOpenApi> apis() {
        List<GroupedOpenApi> groups = new ArrayList<>();
        
        groups.add(GroupedOpenApi.builder()
                .group("user-service")
                .pathsToMatch("/api/users/**", "/api/auth/**")
                .build());
                
        groups.add(GroupedOpenApi.builder()
                .group("approval-service")
                .pathsToMatch("/api/approval/**")
                .build());
                
        groups.add(GroupedOpenApi.builder()
                .group("tenant-service")
                .pathsToMatch("/api/tenant/**")
                .build());

        groups.add(GroupedOpenApi.builder()
                .group("org-service")
                .pathsToMatch("/api/org/**")
                .build());

        groups.add(GroupedOpenApi.builder()
                .group("attendance-service")
                .pathsToMatch("/api/attendance/**")
                .build());

        groups.add(GroupedOpenApi.builder()
                .group("news-service")
                .pathsToMatch("/api/news/**")
                .build());

        groups.add(GroupedOpenApi.builder()
                .group("leave-service")
                .pathsToMatch("/api/leave/**")
                .build());

        groups.add(GroupedOpenApi.builder()
                .group("companyinfo-service")
                .pathsToMatch("/api/companyinfo/**")
                .build());

        groups.add(GroupedOpenApi.builder()
                .group("communication-service")
                .pathsToMatch("/api/communication/**")
                .build());
                
        return groups;
    }
}