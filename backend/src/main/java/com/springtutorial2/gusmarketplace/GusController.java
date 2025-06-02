package com.springtutorial2.gusmarketplace;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gus")
@AllArgsConstructor
public class GusController {

    private final GusService gusService;

    @GetMapping
    public List<ListingDTO> fetchAllListings() {
        return gusService.getAllListings();
    }

    @GetMapping("/category")
    public List<ListingDTO> fetchListingsByCategory(String category) {
        return gusService.getListingsByCategory(category);
    }
    @GetMapping("/title")
    public List<ListingDTO> fetchListingsByTitle(String title) {
        return gusService.getListingsByTitle(title);
    }

    @PostMapping("/create")
    public Listing createListing(@RequestBody Listing listing) {
        return gusService.createListing(listing);
    }

    @PostMapping("/delete/{id}")
    public void deleteListing(@PathVariable String id) {
        gusService.deleteListing(id);
    }
}
