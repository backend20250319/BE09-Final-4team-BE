package com.hermes.orgservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OrgServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrgServiceApplication.class, args);
    }

}
