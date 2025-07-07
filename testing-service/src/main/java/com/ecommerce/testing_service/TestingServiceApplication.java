package com.ecommerce.testing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnbleDiscoveryClient
@SpringBootApplication
public class TestingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestingServiceApplication.class, args);
	}

}
