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


@AllArgsConstructor
@Service
public class GusService {
    private final AmazonS3 s3Client;
    private final GusRepository gusRepository;

    public List<Listing> getAllListings() {

        return gusRepository.findAll();
    }


    public void createListing(Listing listing){

        gusRepository.save(listing);

    }

    public List<Listing> getListingsByCategory(String category) {

        List<Listing> listings = gusRepository.findListingByCategory(category);

        if (listings.isEmpty()) {
            throw new RuntimeException("No listings found for category: " + category);
        }

        return listings;
    }

    public List<Listing> getListingsByTitle(String title) {
        List<Listing> listings = gusRepository.findListingByTitle(title);

        if (listings.isEmpty()) {
            throw new RuntimeException("No listings found for title: " + title);
        }

        return listings;
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
