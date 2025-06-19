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

    public List<ListingDTO> getAllListings() {
        List<Listing> listings =  gusRepository.findAll();

        return listings.stream()
                .map(this::convertToDTO) // Convert each Listing to ListingDTO
                .collect(Collectors.toList());
    }

    private ListingDTO convertToDTO(Listing listing) {
        ListingDTO dto = new ListingDTO();
        dto.setId(listing.getId());
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

        gusRepository.save(listing);

    }

    public List<ListingDTO> getListingsByCategory(String category) {

        List<Listing> listings = gusRepository.findListingByCategory(category);

        if (listings.isEmpty()) {
            throw new RuntimeException("No listings found for category: " + category);
        }

        return listings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ListingDTO> getListingsByTitle(String title) {
        List<Listing> listings = gusRepository.findListingByTitle(title);

        if (listings.isEmpty()) {
            throw new RuntimeException("No listings found for title: " + title);
        }

        return listings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteListing(String id) {
        gusRepository.deleteById(id);
    }

    public Map<String, String> generateUploadUrl(){
        String bucketName = "gus-market-listing-imgs";
        String fileName = UUID.randomUUID().toString() + ".jpg";

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


}
