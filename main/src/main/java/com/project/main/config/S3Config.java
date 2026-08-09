package com.project.main.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.time.Duration;

@Configuration
public class S3Config {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Config(
            @Value("${app.s3.endpoint}") String endpoint,
            @Value("${app.s3.access-key}") String accessKey,
            @Value("${app.s3.secret-key}") String secretKey,
            @Value("${app.s3.bucket-name}") String bucketName) {

        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("app.s3.endpoint is blank");
        }
        if (accessKey == null || accessKey.isBlank()) {
            throw new IllegalStateException("app.s3.access-key is blank");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("app.s3.secret-key is blank");
        }

        this.bucketName = bucketName;

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("ru-central-1"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallAttemptTimeout(Duration.ofSeconds(3))
                        .apiCallTimeout(Duration.ofSeconds(5))
                        .retryPolicy(RetryPolicy.builder().numRetries(2).build())
                        .build())
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return this.s3Client;
    }

    @PostConstruct
    public void initBucket() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            System.out.println("Bucket '" + bucketName + "' is created successfully");
        } catch (BucketAlreadyOwnedByYouException | BucketAlreadyExistsException e) {
            System.out.println("Bucket '" + bucketName + "' already exists");
        } catch (Exception e) {
            System.err.println("Could not check/create bucket '" + bucketName + "': " + e.getMessage());
            return;
        }

        try {
            String policyJson = String.format(
                    "{" +
                            "\"Version\":\"2012-10-17\"," +
                            "\"Statement\":[" +
                            "{" +
                            "\"Effect\":\"Allow\"," +
                            "\"Principal\":\"*\"," +
                            "\"Action\":[\"s3:GetObject\"]," +
                            "\"Resource\":[\"arn:aws:s3:::%s/*\"]" +
                            "}" +
                            "]" +
                            "}", bucketName
            );

            s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .policy(policyJson)
                    .build());

            System.out.println("Download policy successfully applied to bucket: '" + bucketName + "'");
        } catch (Exception e) {
            System.err.println("Could not apply download polity to bucket: '" + bucketName + "'. Error: " + e.getMessage());
        }
    }
}