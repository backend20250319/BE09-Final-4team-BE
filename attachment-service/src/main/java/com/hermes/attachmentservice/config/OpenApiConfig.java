package com.hermes.attachmentservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 설정
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Hermes Attachment Service API",
        description = "파일 업로드 및 다운로드 관리를 위한 REST API",
        version = "1.0.0"
    ),
    security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "JWT Bearer 토큰을 입력하세요"
)
public class OpenApiConfig {
}