package com.eurekaEcommerce.eurekaEcommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaEcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaEcommerceApplication.class, args);
	}

}
