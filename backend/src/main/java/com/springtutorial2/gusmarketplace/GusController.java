package com.springtutorial2.gusmarketplace;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gus")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")

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
    public void createListing(@RequestBody Map<String, String> listingData, HttpServletRequest request) {
        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        // Extract userId from JWT token
        String userId = JwtUtils.extractUserIdFromToken(token);
        if (userId == null) {
            throw new RuntimeException("Unauthorized: Invalid token");
        }

        Listing listing = new Listing(
                null, // id
                userId,
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
            System.err.println("Error creating listing: " + e.getMessage());
            throw new RuntimeException("Failed to create listing: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{id}")
    public void deleteListing(@PathVariable String id, HttpServletRequest request) {
        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        if (token == null) {
            throw new RuntimeException("Unauthorized: No token provided");
        }
        
        // Extract userId and email from JWT token
        String userId = JwtUtils.extractUserIdFromToken(token);
        String userEmail = JwtUtils.extractEmailFromToken(token);
        
        if (userId == null || userEmail == null) {
            throw new RuntimeException("Unauthorized: Invalid token");
        }
        
        try {
            // Use the new method that checks admin or ownership
            gusService.deleteListingIfOwnerOrAdmin(id, userId, userEmail);
        } catch (Exception e) {
            System.err.println("Error deleting listing: " + e.getMessage());
            throw new RuntimeException("Failed to delete listing: " + e.getMessage());
        }
    }

    @GetMapping("/getUploadUrl")
    public Map<String, String> getUploadUrl() {
        return gusService.generateUploadUrl();
    }

    @PostMapping("/contact-seller/{listingId}")
    public ResponseEntity<Map<String, String>> contactSeller(@PathVariable String listingId, 
                             @RequestBody Map<String, String> contactData,
                             HttpServletRequest request) {
        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        if (token == null) {
            throw new RuntimeException("Unauthorized: No token provided");
        }
        
        // Extract buyer info from JWT token
        String buyerEmail = JwtUtils.extractEmailFromToken(token);
        String buyerName = contactData.get("buyerName");
        String message = contactData.get("message");
        
        if (buyerEmail == null) {
            throw new RuntimeException("Unauthorized: Invalid token");
        }
        
        if (message == null || message.trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }
        
        try {
            gusService.sendContactEmailToSeller(listingId, buyerEmail, buyerName, message);
            Map<String, String> response = new java.util.HashMap<>();
            response.put("message", "Email sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error sending contact email: " + e.getMessage());
            e.printStackTrace();
            // The GlobalExceptionHandler will catch this and return proper JSON
            throw new RuntimeException(e.getMessage());
        }
    }
}
