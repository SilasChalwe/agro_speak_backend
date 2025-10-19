package com.nextinnomind.agro_speak_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgroSpeakBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgroSpeakBackendApplication.class, args);
	}

}
