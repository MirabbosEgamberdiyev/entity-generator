package com.example.entity_generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.entity_generator", "com.example.generated"})
@EnableJpaRepositories(basePackages = "com.example.generated.repository")
@EntityScan(basePackages = "com.example.generated.entity")
public class EntityGeneratorApplication {
	public static void main(String[] args) {
		SpringApplication.run(EntityGeneratorApplication.class, args);
	}
}