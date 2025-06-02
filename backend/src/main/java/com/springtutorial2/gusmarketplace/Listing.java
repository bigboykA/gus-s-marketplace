package com.springtutorial2.gusmarketplace;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "listings")
@AllArgsConstructor
public class Listing {
    @Id
    private String id;
    private String userName;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private String price;
    private String condition;
    private String groupMeLink;
}
// The Listing class represents a marketplace listing with fields for
// user name, title, description, category, image URL, price, condition, and a GroupMe link.