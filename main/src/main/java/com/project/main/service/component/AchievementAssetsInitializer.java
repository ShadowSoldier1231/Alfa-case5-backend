package com.project.main.service.component;

import com.project.main.enums.Achievement;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Component
public class AchievementAssetsInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AchievementAssetsInitializer.class);
    private final S3Client s3Client;
    private final String bucketName;

    public AchievementAssetsInitializer(S3Client s3Client,
                                        @Value("${app.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void run(@NotNull ApplicationArguments args) {
        logger.info("Starting achievement assets synchronization with S3...");
        for (Achievement achievement : Achievement.values()) {
            syncAsset(achievement.getIconUrl());
        }
        logger.info("Achievement assets synchronization completed successfully.");
    }

    private void syncAsset(String iconUrl) {
        String cleanPath = iconUrl.startsWith("/") ? iconUrl.substring(1) : iconUrl;

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(cleanPath)
                    .build());
        } catch (NoSuchKeyException e) {
            logger.warn("Asset '{}' not found in S3. Uploading from classpath...", cleanPath);
            uploadFromClasspath(cleanPath);
        } catch (Exception e) {
            logger.error("Failed to check asset '{}' in S3: {}", cleanPath, e.getMessage());
        }
    }

    private void uploadFromClasspath(String cleanPath) {
        ClassPathResource resource = new ClassPathResource(cleanPath);

        if (!resource.exists()) {
            throw new IllegalStateException("CRITICAL: Achievement asset '" + cleanPath + "' is missing in src/main/resources");
        }

        try {
            String contentType = cleanPath.endsWith(".png") ? "image/png" : "image/jpeg";

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(cleanPath)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(resource.getInputStream(), resource.contentLength()));
            logger.info("Successfully uploaded '{}' to S3.", cleanPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read asset '" + cleanPath + "' from classpath", e);
        }
    }
}