package com.hermes.communicationservice.config;

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
        title = "Hermes Communication Service API",
        description = """
            공지사항과 결재 알림을 위한 REST API 및 WebSocket 실시간 통신
            
            ## 주요 기능
            - 공지사항 알림
            - 결재 알림
            - WebSocket 실시간 알림 (상세 정보는 'WebSocket 정보' 섹션 참조)
            """,
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