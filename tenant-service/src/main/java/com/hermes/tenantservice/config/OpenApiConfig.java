package com.hermes.tenantservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 설정
 */
@Configuration
@OpenAPIDefinition(
    info = @io.swagger.v3.oas.annotations.info.Info(
        title = "Hermes Tenant Service API",
        description = "테넌트 관리 및 스키마 관리를 위한 REST API",
        version = "1.0.0"
    )
)
public class OpenApiConfig {
}
