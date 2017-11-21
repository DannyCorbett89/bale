package com.dc.bale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories("com.dc.bale.database")
public class BaleApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaleApplication.class, args);
	}
}
