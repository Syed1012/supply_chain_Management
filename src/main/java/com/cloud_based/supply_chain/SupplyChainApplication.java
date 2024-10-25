package com.cloud_based.supply_chain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class SupplyChainApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load(); // Load the .env file
        System.setProperty("MONGO_URI", dotenv.get("MONGO_URI"));
        System.setProperty("MONGO_DB", dotenv.get("MONGO_DB"));
		SpringApplication.run(SupplyChainApplication.class, args);
	}

	@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
