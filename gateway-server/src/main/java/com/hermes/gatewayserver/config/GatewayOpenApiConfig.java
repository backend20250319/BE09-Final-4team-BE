package com.hermes.gatewayserver.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * Gateway OpenAPI 통합 설정
 */
@Configuration
public class GatewayOpenApiConfig {

    @Bean
    public CommandLineRunner openApiGroups(
            RouteDefinitionLocator locator,
            SwaggerUiConfigProperties swaggerUiConfigProperties) {
        return args -> {
            Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();
            
            // 각 서비스의 OpenAPI 문서 URL 추가
            urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("user-service", "/v3/api-docs/user-service", "user-service"));
            urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("approval-service", "/v3/api-docs/approval-service", "approval-service"));
            urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("tenant-service", "/v3/api-docs/tenant-service", "tenant-service"));
            urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("org-service", "/v3/api-docs/org-service", "org-service"));
            urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("attendance-service", "/v3/api-docs/attendance-service", "attendance-service"));
            urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("news-service", "/v3/api-docs/news-service", "news-service"));
            urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("leave-service", "/v3/api-docs/leave-service", "leave-service"));
            urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("companyinfo-service", "/v3/api-docs/companyinfo-service", "companyinfo-service"));
            urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("communication-service", "/v3/api-docs/communication-service", "communication-service"));
            urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("attachment-service", "/v3/api-docs/attachment-service", "attachment-service"));
            
            swaggerUiConfigProperties.setUrls(urls);
        };
    }
}