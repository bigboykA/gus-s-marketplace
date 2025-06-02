package com.springtutorial2.gusmarketplace;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class GusService {

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


    public Listing createListing(Listing listing){
        // Save the listing to the database
        return gusRepository.save(listing);

    }

    public List<ListingDTO> getListingsByCategory(String category) {
        // Fetch listings by category from the repository
        List<Listing> listings = gusRepository.findListingByCategory(category);

        if (listings.isEmpty()) {
            throw new RuntimeException("No listings found for category: " + category);
        }

        return listings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ListingDTO> getListingsByTitle(String title) {
        // Fetch listings by title from the repository
        List<Listing> listings = gusRepository.findListingByTitle(title);

        if (listings.isEmpty()) {
            throw new RuntimeException("No listings found for title: " + title);
        }

        return listings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteListing(String id) {
        // Delete the listing from the database
        gusRepository.deleteById(id);
    }


}
