package com.hermes.approvalservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 설정
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Hermes Approval Service API",
        description = "결재 문서 및 템플릿 관리를 위한 REST API",
        version = "1.0.0"
    )
)
public class OpenApiConfig {
}