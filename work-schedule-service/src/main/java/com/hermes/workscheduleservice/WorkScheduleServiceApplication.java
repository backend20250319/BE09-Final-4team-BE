package com.hermes.workscheduleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WorkScheduleServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkScheduleServiceApplication.class, args);
	}

}
