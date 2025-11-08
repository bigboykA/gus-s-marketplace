package com.springtutorial2.gusmarketplace;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class GusMarketplaceApplication {



    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure().load();
            String mongoUri = dotenv.get("MONGO_URI");
            if (mongoUri != null && !mongoUri.isEmpty()) {
                System.setProperty("MONGO_URI", mongoUri);
            }
        } catch (Exception e) {
            // If .env file doesn't exist, continue without MongoDB
            System.out.println("Warning: .env file not found or could not be loaded. Continuing without MongoDB connection.");
        }

        SpringApplication.run(GusMarketplaceApplication.class, args);
    }


}
