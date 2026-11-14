package com.app_ecom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO) // stable JSON shape for Page responses
@EnableScheduling // transactional outbox publisher
public class EcomApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomApplication.class, args);
	}
}
