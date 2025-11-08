package com.springtutorial2.gusmarketplace;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class GusService {
    private final AmazonS3 s3Client;
    private final GusRepository gusRepository;
    private final MailgunService mailgunService;

    public List<ListingDTO> getAllListings() {
        try {
            List<Listing> listings = gusRepository.findAll();
            return listings.stream()
                    .map(this::convertToDTO) // Convert each Listing to ListingDTO
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching listings (MongoDB may not be connected): " + e.getMessage());
            return List.of(); // Return empty list if MongoDB is not available
        }
    }

    private ListingDTO convertToDTO(Listing listing) {
        ListingDTO dto = new ListingDTO();
        dto.setId(listing.getId());
        dto.setUserId(listing.getUserId()); // Include userId for frontend filtering and ownership checks
        dto.setTitle(listing.getTitle());
        dto.setImageUrl(listing.getImageUrl());
        dto.setDescription(listing.getDescription());
        dto.setPrice(listing.getPrice());
        dto.setCondition(listing.getCondition());
        dto.setGroupMeLink(listing.getGroupMeLink());
        dto.setCategory(listing.getCategory());

        return dto;
    }


    public void createListing(Listing listing){
        // Validate that userName (seller email) is a valid email address
        if (listing.getUserName() != null && !listing.getUserName().isEmpty()) {
            if (!isValidEmail(listing.getUserName())) {
                throw new RuntimeException("Invalid email address format: \"" + listing.getUserName() + "\". Please provide a valid email address.");
            }
        } else {
            throw new RuntimeException("Email address is required to create a listing.");
        }
        
        // Validate that groupMeLink is provided and is a valid GroupMe link
        if (listing.getGroupMeLink() == null || listing.getGroupMeLink().trim().isEmpty()) {
            throw new RuntimeException("GroupMe link is required to create a listing.");
        }
        
        if (!isValidGroupMeLink(listing.getGroupMeLink())) {
            throw new RuntimeException("Invalid GroupMe link format: \"" + listing.getGroupMeLink() + "\". Please provide a valid GroupMe link (e.g., https://groupme.com/join_group/... or groupme://join_group/...).");
        }
        
        try {
            gusRepository.save(listing);
        } catch (Exception e) {
            System.err.println("Error creating listing (MongoDB may not be connected): " + e.getMessage());
            throw new RuntimeException("Failed to create listing. MongoDB may not be connected: " + e.getMessage());
        }
    }
    
    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Basic email validation: must contain @ and have at least one character before and after @
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    // Helper method to validate GroupMe link - just check if "groupme" is in the text (case insensitive)
    private boolean isValidGroupMeLink(String link) {
        if (link == null || link.trim().isEmpty()) {
            return false;
        }
        // Simply check if "groupme" appears in the link (case insensitive)
        // This allows profile links, join_group links, and other GroupMe URLs
        return link.toLowerCase().contains("groupme");
    }

    public List<ListingDTO> getListingsByCategory(String category) {
        try {
            List<Listing> listings = gusRepository.findListingByCategory(category);
            return listings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching listings by category (MongoDB may not be connected): " + e.getMessage());
            return List.of(); // Return empty list if MongoDB is not available
        }
    }

    public List<ListingDTO> getListingsByTitle(String title) {
        try {
            List<Listing> listings = gusRepository.findListingByTitle(title);
            return listings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching listings by title (MongoDB may not be connected): " + e.getMessage());
            return List.of(); // Return empty list if MongoDB is not available
        }
    }

    public void deleteListing(String id) {
        try {
            gusRepository.deleteById(id);
        } catch (Exception e) {
            System.err.println("Error deleting listing (MongoDB may not be connected): " + e.getMessage());
            throw new RuntimeException("Failed to delete listing. MongoDB may not be connected: " + e.getMessage());
        }
    }

    public void deleteListingIfOwner(String id, String userId) {
        var listingOpt = gusRepository.findById(id);
        if (listingOpt.isEmpty()) {
            throw new RuntimeException("Listing not found");
        }
        var listing = listingOpt.get();
        if (listing.getUserId() != null && listing.getUserId().equals(userId)) {
            gusRepository.deleteById(id);
            return;
        }
        throw new RuntimeException("Forbidden: cannot delete another user's listing");
    }

    public void deleteListingIfOwnerOrAdmin(String id, String userId, String userEmail) {
        var listingOpt = gusRepository.findById(id);
        if (listingOpt.isEmpty()) {
            throw new RuntimeException("Listing not found");
        }
        var listing = listingOpt.get();
        
        // Check if user is admin
        boolean isAdmin = "mahatnitai@gmail.com".equalsIgnoreCase(userEmail);
        
        // Check if user owns the listing
        boolean isOwner = listing.getUserId() != null && listing.getUserId().equals(userId);
        
        if (isAdmin || isOwner) {
            gusRepository.deleteById(id);
            return;
        }
        throw new RuntimeException("Forbidden: cannot delete another user's listing");
    }

    public Map<String, String> generateUploadUrl(){
        String bucketName = "gus-market-listing-imgs";
        String fileName = UUID.randomUUID().toString() + ".jpg";

        // Generate presigned URL with image/jpeg as base type
        // The client will send the actual file's MIME type in the PUT request
        // S3 will accept it as long as it's a valid image format
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName)
                .withMethod(com.amazonaws.HttpMethod.PUT)
                .withExpiration(Date.from(Instant.now().plus(15, ChronoUnit.HOURS)))
                .withContentType("image/jpeg");

        URL url = s3Client.generatePresignedUrl(request);

        // Return the upload and file URLs
        return Map.of(
                "uploadUrl", url.toString(),
                "fileUrl", "https://" + bucketName + ".s3.us-east-2.amazonaws.com/" + fileName
            );
    }

    public void sendContactEmailToSeller(String listingId, String buyerEmail, String buyerName, String message) {
        // Find the listing to get seller's email
        var listingOpt = gusRepository.findById(listingId);
        if (listingOpt.isEmpty()) {
            throw new RuntimeException("Listing not found");
        }
        
        Listing listing = listingOpt.get();
        String sellerEmail = listing.getUserName(); // Seller's email is stored in userName field
        
        System.out.println("Attempting to send email to seller: " + sellerEmail);
        System.out.println("From buyer: " + buyerEmail + " (" + buyerName + ")");
        System.out.println("Listing: " + listing.getTitle());
        
        if (sellerEmail == null || sellerEmail.isEmpty()) {
            throw new RuntimeException("Seller email not found for this listing. The listing may have been created with invalid data.");
        }
        
        // Validate email format (more comprehensive check)
        if (!isValidEmail(sellerEmail)) {
            throw new RuntimeException("Invalid seller email format: \"" + sellerEmail + "\". The listing was created with an invalid email address. Please contact the seller through other means or ask them to update their listing.");
        }
        
        // Send email via Mailgun
        mailgunService.sendContactEmail(sellerEmail, buyerEmail, buyerName, listing.getTitle(), message);
    }

}
