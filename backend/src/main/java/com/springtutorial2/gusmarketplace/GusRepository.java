package com.springtutorial2.gusmarketplace;

import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface GusRepository extends MongoRepository<Listing, String> {
    // This interface extends MongoRepository to provide CRUD operations for Listing entities.
    // It uses String as the type of the ID field.
    // Additional query methods can be defined here if needed.
    // For example, you could add methods to find listings by userName, category, etc.

    List<Listing> findListingByCategory(String category);
    List<Listing> findListingByTitle(String title);

}
