package com.dreamhire.candidate_scores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CandidateScoresApplication {

	public static void main(String[] args) {
		SpringApplication.run(CandidateScoresApplication.class, args);
	}

}
