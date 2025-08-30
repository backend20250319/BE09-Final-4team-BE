package com.hermes.attachment.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.hermes.attachment")
@ComponentScan(basePackages = "com.hermes.attachment")
@ConditionalOnProperty(name = "hermes.attachment.enabled", matchIfMissing = true)
public class AttachmentClientAutoConfiguration {
}