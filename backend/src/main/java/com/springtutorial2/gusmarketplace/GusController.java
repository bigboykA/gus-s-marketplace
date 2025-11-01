package com.springtutorial2.gusmarketplace;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gus")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")

public class GusController {

    private final GusService gusService;

    @GetMapping
    public List<Listing> fetchAllListings() {
        return gusService.getAllListings();
    }


    @GetMapping("/category")
    public List<Listing> fetchListingsByCategory(@RequestParam("categoryName") String categoryName) {
        return gusService.getListingsByCategory(categoryName);
    }

    @GetMapping("/title")
    public List<Listing> fetchListingsByTitle(@RequestParam("titleName") String titleName) {
        return gusService.getListingsByTitle(titleName);
    }

    @PostMapping("/create")
    public void createListing(@RequestBody Map<String, String> listingData) {

        Listing listing = new Listing(
                null, // ID will be auto-generated
                listingData.get("userName"),
                listingData.get("title"),
                listingData.get("description"),
                listingData.get("category"),
                listingData.get("imgUrl"),
                listingData.get("price"),
                listingData.get("condition"),
                listingData.get("groupMeLink")

        );
        try{
            gusService.createListing(listing);

        }catch (Exception e){
            // Handle the exception, e.g., log it or return an error response
            System.err.println("Error creating listing: " + e.getMessage());
            throw new RuntimeException("Failed to create listing: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{id}")
    public void deleteListing(@PathVariable String id) {
        gusService.deleteListing(id);
    }

    @GetMapping("/getUploadUrl")
    public Map<String, String> getUploadUrl() {
        return gusService.generateUploadUrl();
    }
}
