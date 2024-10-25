package com.cloud_based.supply_chain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class SupplyChainApplication {

    public static void main(String[] args) {
        try {
            // Only load .env file if it exists
            Dotenv dotenv = Dotenv.configure()
                                .ignoreIfMissing()
                                .load();
            
            // Set properties only if they're not already set as environment variables
            if (System.getenv("MONGO_URI") == null && dotenv.get("MONGO_URI") != null) {
                System.setProperty("MONGO_URI", dotenv.get("MONGO_URI"));
            }
            if (System.getenv("MONGO_DB") == null && dotenv.get("MONGO_DB") != null) {
                System.setProperty("MONGO_DB", dotenv.get("MONGO_DB"));
            }
        } catch (Exception e) {
            // Log the error but continue application startup
            System.out.println("Warning: Could not load .env file. Using default or environment variables.");
        }
        
        SpringApplication.run(SupplyChainApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}