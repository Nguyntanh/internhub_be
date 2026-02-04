package com.example.internhub_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InternhubBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternhubBeApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> {
			System.out.println("Hello from Internhub BE Application! This is a demo.");
		};
	}
}
