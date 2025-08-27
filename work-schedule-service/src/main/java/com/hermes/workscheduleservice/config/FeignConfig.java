package com.hermes.workscheduleservice.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.hermes.workscheduleservice.client")
public class FeignConfig {
} 