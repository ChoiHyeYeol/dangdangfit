package com.inside.ddf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DdfApplication {

	public static void main(String[] args) {
		SpringApplication.run(DdfApplication.class, args);
	}

}
