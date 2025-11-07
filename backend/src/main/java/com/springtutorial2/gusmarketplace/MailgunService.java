package com.springtutorial2.gusmarketplace;

import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

@Service
public class MailgunService {
    
    private static final String MAILGUN_API_KEY = "ENV_VAR_PLACEHOLDER";
    private static final String MAILGUN_DOMAIN = "gusmarketplace.com";
    private static final String MAILGUN_BASE_URL = "https://api.mailgun.net/v3/" + MAILGUN_DOMAIN + "/messages";
    private static final String FROM_EMAIL = "postmaster@" + MAILGUN_DOMAIN;
    
    public void sendContactEmail(String sellerEmail, String buyerEmail, String buyerName, 
                                 String listingTitle, String message) {
        try {
            URL url = new URL(MAILGUN_BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set up the connection
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", 
                "Basic " + Base64.getEncoder().encodeToString(("api:" + MAILGUN_API_KEY).getBytes()));
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            // Build the email content
            String subject = "New Message About Your Listing: " + listingTitle;
            String emailBody = String.format(
                "Hello,\n\n" +
                "You have received a new message from a buyer interested in your listing.\n\n" +
                "Listing: %s\n" +
                "Buyer: %s (%s)\n\n" +
                "Message:\n%s\n\n" +
                "You can reply directly to this email to contact the buyer.",
                listingTitle, buyerName, buyerEmail, message
            );
            
            // Build the form data
            String formData = String.format(
                "from=%s&to=%s&subject=%s&text=%s",
                URLEncoder.encode("GUS Marketplace <" + FROM_EMAIL + ">", StandardCharsets.UTF_8),
                URLEncoder.encode(sellerEmail, StandardCharsets.UTF_8),
                URLEncoder.encode(subject, StandardCharsets.UTF_8),
                URLEncoder.encode(emailBody, StandardCharsets.UTF_8)
            );
            
            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = formData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Check the response
            int responseCode = connection.getResponseCode();
            String responseBody = "";
            
            // Read response body (success or error)
            try {
                java.io.InputStream inputStream = null;
                if (responseCode >= 200 && responseCode < 300) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                    // If error stream is null, try input stream
                    if (inputStream == null) {
                        try {
                            inputStream = connection.getInputStream();
                        } catch (Exception e) {
                            // If both fail, we'll use empty response body
                            System.err.println("Could not read error stream or input stream: " + e.getMessage());
                        }
                    }
                }
                
                if (inputStream != null) {
                    Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                    responseBody = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();
                } else {
                    responseBody = "No response body available";
                }
            } catch (Exception e) {
                System.err.println("Error reading response: " + e.getMessage());
                e.printStackTrace();
                responseBody = "Error reading response: " + e.getMessage();
            }
            
            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("Email sent successfully to " + sellerEmail);
                System.out.println("Response: " + responseBody);
            } else {
                System.err.println("Failed to send email. Response code: " + responseCode);
                System.err.println("Response body: " + responseBody);
                System.err.println("Seller email: " + sellerEmail);
                System.err.println("Buyer email: " + buyerEmail);
                System.err.println("From email: " + FROM_EMAIL);
                System.err.println("Domain: " + MAILGUN_DOMAIN);
                
                // Provide more helpful error messages
                String errorMsg = "Failed to send email. Response code: " + responseCode;
                if (responseCode == 401) {
                    errorMsg += ". Authentication failed. Please check your API key.";
                } else if (responseCode == 403) {
                    errorMsg += ". Domain not verified or DNS records not configured. Please verify your DNS settings in Mailgun.";
                } else if (responseCode == 400) {
                    errorMsg += ". Bad request. Response: " + responseBody;
                } else {
                    errorMsg += ". Response: " + responseBody;
                }
                
                throw new RuntimeException(errorMsg);
            }
            
            connection.disconnect();
            
        } catch (RuntimeException e) {
            // Re-throw runtime exceptions as-is
            throw e;
        } catch (Exception e) {
            System.err.println("Error sending email via Mailgun: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}

