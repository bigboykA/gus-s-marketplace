package com.springtutorial2.gusmarketplace;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * Creates a listing with image moderation before S3 upload.
     * This endpoint accepts multipart/form-data and ensures images are moderated
     * before being uploaded to S3, preventing inappropriate content from being
     * stored.
     * 
     * @param userName    User name
     * @param title       Listing title
     * @param description Listing description
     * @param category    Listing category
     * @param price       Listing price
     * @param condition   Item condition
     * @param groupMeLink GroupMe link
     * @param imageFile   Image file (optional)
     * @return Response with created listing or error message
     */
    @PostMapping(value = "/create", consumes = { "multipart/form-data" })
    public ResponseEntity<?> createListingWithImage(
            @RequestParam("userName") String userName,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("price") String price,
            @RequestParam("condition") String condition,
            @RequestParam(value = "groupMeLink", required = false) String groupMeLink,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            // Create listing with moderation checks
            Listing createdListing = gusService.createListingWithImage(
                    userName,
                    title,
                    description,
                    category,
                    price,
                    condition,
                    groupMeLink,
                    imageFile);

            // Return the created listing
            return ResponseEntity.status(HttpStatus.CREATED).body(createdListing);
        } catch (ContentSafetyService.ContentModerationException e) {
            // Handle content moderation exception with a clear error message
            System.err.println("Content moderation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Content moderation failed", "message",
                            e.getMessage() != null ? e.getMessage() : "Content moderation failed"));
        } catch (Exception e) {
            // Handle other exceptions
            System.err.println("Error creating listing: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create listing", "message",
                            e.getMessage() != null ? e.getMessage() : "An unknown error occurred"));
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
