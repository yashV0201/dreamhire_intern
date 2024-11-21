package com.dreamhire.pre_filtering_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PreFilteringServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PreFilteringServiceApplication.class, args);
	}

}
