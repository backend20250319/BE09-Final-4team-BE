package com.hermes.ftpspringbootstarter.config;


import com.hermes.ftpspringbootstarter.properties.FtpProperties;
import com.hermes.ftpspringbootstarter.service.FtpService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FtpProperties.class)
@ConditionalOnProperty(prefix = "ftp", name = "enabled", havingValue = "true")
public class FtpAutoConfiguration {

  @Bean
  public FtpService ftpService(FtpProperties properties) {
    return new FtpService(properties);
  }

}
