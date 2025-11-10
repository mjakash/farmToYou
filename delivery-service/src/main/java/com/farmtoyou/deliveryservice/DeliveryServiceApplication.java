package com.farmtoyou.deliveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeliveryServiceApplication {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(DeliveryServiceApplication.class);
		app.setWebApplicationType(WebApplicationType.SERVLET); // To prevent Web/Webflux conflict
		app.run(args);
	}
}