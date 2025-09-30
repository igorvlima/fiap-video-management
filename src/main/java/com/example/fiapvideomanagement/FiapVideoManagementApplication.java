package com.example.fiapvideomanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FiapVideoManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(FiapVideoManagementApplication.class, args);
	}

}
