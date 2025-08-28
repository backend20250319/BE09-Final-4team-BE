package com.hermes.communicationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.hermes.communicationservice",
    "com.hermes.ftpstarter"})
@EnableFeignClients(basePackages = "com.hermes.userserviceclient")
@EnableJpaAuditing
public class CommunicationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CommunicationServiceApplication.class, args);
  }

}

