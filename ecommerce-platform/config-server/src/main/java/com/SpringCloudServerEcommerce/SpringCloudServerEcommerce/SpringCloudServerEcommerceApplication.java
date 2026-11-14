package com.SpringCloudServerEcommerce.SpringCloudServerEcommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class SpringCloudServerEcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudServerEcommerceApplication.class, args);
	}

}
