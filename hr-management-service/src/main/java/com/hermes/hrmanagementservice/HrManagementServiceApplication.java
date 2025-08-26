package com.hermes.hrmanagementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan("com.hermes.hrmanagementservice.entity")
@EnableJpaRepositories("com.hermes.hrmanagementservice.repository")
public class HrManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrManagementServiceApplication.class, args);
    }
}
