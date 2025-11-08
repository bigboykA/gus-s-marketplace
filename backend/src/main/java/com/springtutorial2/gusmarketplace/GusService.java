package com.springtutorial2.gusmarketplace;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.springtutorial2.gusmarketplace.ContentSafetyService.ContentModerationException;

import java.io.IOException;
import java.io.InputStream;
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
    private final ContentSafetyService contentSafetyService;

    public List<Listing> getAllListings() {

        return gusRepository.findAll();
    }

    /**
     * Creates a listing with image moderation before S3 upload.
     * This ensures inappropriate images are never uploaded to S3.
     *
     * @param userName    User name
     * @param title       Listing title
     * @param description Listing description
     * @param category    Listing category
     * @param price       Listing price
     * @param condition   Item condition
     * @param groupMeLink GroupMe link
     * @param imageFile   Image file (can be null)
     * @return The created listing
     * @throws ContentModerationException if content moderation fails
     */
    public Listing createListingWithImage(
            String userName,
            String title,
            String description,
            String category,
            String price,
            String condition,
            String groupMeLink,
            MultipartFile imageFile) throws ContentModerationException, IOException {

        String imageUrl = null;

        // If image is provided, moderate it first, then upload to S3
        if (imageFile != null && !imageFile.isEmpty()) {
            // Moderate image before uploading to S3
            byte[] imageBytes = imageFile.getBytes();
            if (!contentSafetyService.moderateImageFromBytes(imageBytes)) {
                throw new ContentModerationException(
                        "Listing contains inappropriate image content and cannot be created.");
            }

            // Upload to S3 only after moderation passes
            imageUrl = uploadImageToS3(imageFile);
        }

        // Moderate text content (title and description)
        String textToModerate = (title != null ? title : "") + " " +
                (description != null ? description : "");

        if (!textToModerate.trim().isEmpty()) {
            if (!contentSafetyService.moderateText(textToModerate)) {
                throw new ContentModerationException(
                        "Listing contains inappropriate text content and cannot be created.");
            }
        }

        // Create and save listing
        Listing listing = new Listing(
                null, // ID will be auto-generated
                userName,
                title,
                description,
                category,
                imageUrl,
                price,
                condition,
                groupMeLink);
        
        gusRepository.save(listing);
        return listing;
    }

    /**
     * Uploads an image file directly to S3 and returns the public URL
     */
    private String uploadImageToS3(MultipartFile imageFile) throws IOException {
        String bucketName = "gus-market-listing-imgs";

        // Preserve original file extension or default to .jpg
        String originalFilename = imageFile.getOriginalFilename();
        String extension = ".jpg"; // Default extension
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(imageFile.getContentType());
        metadata.setContentLength(imageFile.getSize());

        try (InputStream inputStream = imageFile.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    inputStream,
                    metadata);

            s3Client.putObject(putObjectRequest);
        }

        return "https://" + bucketName + ".s3.us-east-2.amazonaws.com/" + fileName;
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

    public Map<String, String> generateUploadUrl() {
        String bucketName = "gus-market-listing-imgs";
        String fileName = UUID.randomUUID().toString() + ".jpg";

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName)
                .withMethod(com.amazonaws.HttpMethod.PUT)
                .withExpiration(Date.from(Instant.now().plus(15, ChronoUnit.HOURS)))
                .withContentType("image/jpeg");

        URL url = s3Client.generatePresignedUrl(request);

        // Return the upload and file URLs
        String uploadUrl = url != null ? url.toString() : "";
        String fileUrl = "https://" + bucketName + ".s3.us-east-2.amazonaws.com/" + fileName;
        return Map.of(
                "uploadUrl", uploadUrl,
                "fileUrl", fileUrl);
    }

}
