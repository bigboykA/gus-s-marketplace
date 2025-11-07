package com.springtutorial2.gusmarketplace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;

public class JwtUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String extractUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            // Supabase stores user ID in "sub" claim
            return jsonNode.has("sub") ? jsonNode.get("sub").asText() : null;
        } catch (Exception e) {
            System.err.println("Error extracting user ID from token: " + e.getMessage());
            return null;
        }
    }

    public static String extractEmailFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            // Supabase stores email in "email" claim
            return jsonNode.has("email") ? jsonNode.get("email").asText() : null;
        } catch (Exception e) {
            System.err.println("Error extracting email from token: " + e.getMessage());
            return null;
        }
    }
}

