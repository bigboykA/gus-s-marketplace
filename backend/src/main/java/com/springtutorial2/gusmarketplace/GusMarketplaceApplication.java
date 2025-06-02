package com.springtutorial2.gusmarketplace;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class GusMarketplaceApplication {



    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        System.setProperty("MONGO_URI", dotenv.get("MONGO_URI"));

        SpringApplication.run(GusMarketplaceApplication.class, args);
    }


    @Bean
    CommandLineRunner runner(GusService gusService) {
        return args -> {
            // You can initialize some data here if needed
            // For example, you could create some sample listings
            // gusService.createListing(new Listing("Sample Title", "Sample Description", "Sample Category", "http://example.com/image.jpg", "10.00", "New", "http://groupme.link"));
            Listing sampleListing = new Listing(
                "1",
                "Gus",
                "Sample Title",
                "Sample Description",
                "Sample Category",
                "http://example.com/image.jpg",
                "10.00",
                "New",
                "http://groupme.link"
            );
            gusService.createListing(sampleListing);
        };
    }
}
