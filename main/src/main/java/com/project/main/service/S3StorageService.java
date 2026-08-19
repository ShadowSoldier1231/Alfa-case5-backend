package com.project.main.service;



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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class S3StorageService {
    private final S3Client s3Client;
    private final String bucketName;
    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".jpg", ".jpeg", ".png", ".webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public S3StorageService(S3Client s3Client, @Value("${app.s3.bucket-name}") String bucketName) {
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

        boolean allowPdf = "cases/pdfs".equals(folderPrefix);

        validateFileContent(file, extension, allowPdf);

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

    private void validateFileContent(MultipartFile file, String extension, boolean allowPdf) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = is.readNBytes(12);
            int read = header.length;
            if (read < 4) {
                throw new BadRequestException("Invalid file content");
            }

            String detectedType = null;

            if (header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46) {
                detectedType = "pdf";
            }
            else if (header[0] == (byte)0xFF && header[1] == (byte)0xD8 && header[2] == (byte)0xFF) {
                detectedType = "jpeg";
            }
            else if (header[0] == (byte)0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                detectedType = "png";
            }
            else if (read >= 12 && header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                    && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
                detectedType = "webp";
            }

            if (detectedType == null) {
                throw new BadRequestException("File content does not match allowed formats");
            }

            if ("pdf".equals(detectedType) && !allowPdf) {
                throw new BadRequestException("PDF files are not allowed for this upload");
            }

            String extLower = extension.toLowerCase();
            boolean extensionMatches =
                    (extLower.equals(".pdf") && "pdf".equals(detectedType)) ||
                            ((extLower.equals(".jpg") || extLower.equals(".jpeg")) && "jpeg".equals(detectedType)) ||
                            (extLower.equals(".png") && "png".equals(detectedType)) ||
                            (extLower.equals(".webp") && "webp".equals(detectedType));

            if (!extensionMatches) {
                throw new BadRequestException("File extension does not match content");
            }

        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to read file content");
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