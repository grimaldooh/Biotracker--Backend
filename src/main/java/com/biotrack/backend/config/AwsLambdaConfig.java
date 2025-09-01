package com.biotrack.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

import java.time.Duration;

@Configuration
public class AwsLambdaConfig {
    
    @Value("${aws.lambda.region:us-east-2}")
    private String region;
    
    @Value("${aws.access-key-id:}")
    private String accessKeyId;
    
    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;
    
    @Bean
    public LambdaClient lambdaClient() {
        return LambdaClient.builder()
                .region(Region.of(region))
                .credentialsProvider(createCredentialsProvider())
                .overrideConfiguration(builder -> 
                    builder.apiCallTimeout(Duration.ofSeconds(40))
                           .apiCallAttemptTimeout(Duration.ofSeconds(35)))
                .build();
    }
    
    private AwsCredentialsProvider createCredentialsProvider() {
        // Si hay credenciales explícitas, usarlas; sino usar DefaultCredentialsProvider
        if (!accessKeyId.isEmpty() && !secretAccessKey.isEmpty()) {
            return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            );
        }
        
        // Usará variables de entorno, IAM roles, etc.
        return DefaultCredentialsProvider.create();
    }
}