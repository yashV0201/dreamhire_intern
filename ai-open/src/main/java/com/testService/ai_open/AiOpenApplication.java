package com.testService.ai_open;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AiOpenApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiOpenApplication.class, args);
	}

}
