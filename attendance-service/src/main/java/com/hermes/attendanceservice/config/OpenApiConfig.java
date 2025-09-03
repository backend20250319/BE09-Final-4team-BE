package com.hermes.attendanceservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI attendanceServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Attendance Service API")
                        .description("근태 관리 서비스 API 문서")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hermes Team")
                                .email("hermes@company.com")
                                .url("https://hermes.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.hermes.com")
                                .description("Production Server")
                ));
    }
} 