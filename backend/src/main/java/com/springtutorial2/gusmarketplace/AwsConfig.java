package com.springtutorial2.gusmarketplace;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {

    @Bean
    public AmazonS3 amazonS3() {
        try {
            Dotenv dotenv = Dotenv.configure().load();
            String accessKey = dotenv.get("AWS_ACCESS_KEY_ID");
            String secretKey = dotenv.get("AWS_SECRET_ACCESS_KEY");
            
            if (accessKey == null || secretKey == null || accessKey.isEmpty() || secretKey.isEmpty() || 
                accessKey.equals("placeholder") || secretKey.equals("placeholder")) {
                // Return a dummy client that will fail gracefully when used
                // This allows the app to start without real AWS credentials
                return AmazonS3ClientBuilder.standard()
                        .withRegion("us-east-2")
                        .withCredentials(new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials("placeholder", "placeholder")))
                        .build();
            }
            
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            return AmazonS3ClientBuilder.standard()
                    .withRegion("us-east-2")
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .build();
        } catch (Exception e) {
            // If .env file doesn't exist or any error, return placeholder client
            return AmazonS3ClientBuilder.standard()
                    .withRegion("us-east-2")
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials("placeholder", "placeholder")))
                    .build();
        }
    }
}