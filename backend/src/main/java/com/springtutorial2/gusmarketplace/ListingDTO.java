package com.springtutorial2.gusmarketplace;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListingDTO {
    private String id;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private String price;
    private String condition;
    private String groupMeLink;
}
