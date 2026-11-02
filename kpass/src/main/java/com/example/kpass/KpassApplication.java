package com.example.kpass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KpassApplication {

	public static void main(String[] args) {
		SpringApplication.run(KpassApplication.class, args);
	}

}
