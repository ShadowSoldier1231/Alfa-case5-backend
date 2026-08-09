package com.project.main.service;


import com.project.main.controller.AdminApiController;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);


    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".jpg", ".jpeg", ".png", ".webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public S3StorageService(S3Client s3Client,
                            @Value("${app.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }


    public String uploadFile(MultipartFile file, String folderPrefix) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size cannot exceed 5 MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isAllowedExtension(originalFilename)) {
            throw new BadRequestException("Invalid file extension. Allowed Extensions: " + ALLOWED_EXTENSIONS);
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        String s3Key = folderPrefix + "/" + uniqueFilename;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));


            return s3Key;

        } catch (Exception e) {
            throw new InternalServerErrorException("Could not upload file: " + e.getMessage());
        }
    }

    private boolean isAllowedExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return ALLOWED_EXTENSIONS.stream().anyMatch(lowerCaseFilename::endsWith);
    }
    public void deleteFile(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build());
        } catch (Exception e) {

            logger.error("Could not delete file from S3: " + s3Key + ". Error: " + e.getMessage());
        }
    }
}