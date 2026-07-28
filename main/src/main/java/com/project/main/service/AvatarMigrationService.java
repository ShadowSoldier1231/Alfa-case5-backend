package com.project.main.service;



import com.project.main.dto.ByteArrayMultipartFile;
import com.project.main.model.*;

import com.project.main.repository.UserAvatarRepository;
import com.project.main.repository.UserDataRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AvatarMigrationService {

    private final UserAvatarRepository userAvatarRepository;
    private final UserDataRepository userDataRepository;
    private final S3StorageService s3StorageService;

    public AvatarMigrationService(UserAvatarRepository userAvatarRepository,
                                  UserDataRepository userDataRepository,
                                  S3StorageService s3StorageService) {
        this.userAvatarRepository = userAvatarRepository;
        this.userDataRepository = userDataRepository;
        this.s3StorageService = s3StorageService;
    }


    public Map<String, Object> migrateAvatarsToS3() {
        List<UserAvatar> avatars = userAvatarRepository.findAll();

        int successCount = 0;
        int errorCount = 0;
        int skippedCount = 0;

        for (UserAvatar avatar : avatars) {
            try {
                Long userId = avatar.getUserId();

                UserData userData = userDataRepository.findById(userId).orElse(null);
                if (userData == null) {
                    skippedCount++;
                    continue;
                }

                if (userData.getAvatarUrl() != null && !userData.getAvatarUrl().isBlank()) {
                    skippedCount++;
                    continue;
                }

                byte[] imageData = avatar.getPictureData();
                if (imageData == null || imageData.length == 0) {
                    skippedCount++;
                    continue;
                }


                MultipartFile multipartFile = new ByteArrayMultipartFile(
                        "avatar",
                        "avatar_" + userId + ".jpg",
                        "image/jpeg",
                        imageData
                );


                String avatarKey = s3StorageService.uploadFile(multipartFile, "avatars");


                userData.setAvatarUrl(avatarKey);
                userDataRepository.save(userData);

                successCount++;
                System.out.println("Мигрирована аватарка пользователя ID=" + userId + " -> " + avatarKey);

            } catch (Exception e) {
                errorCount++;
                System.err.println("Ошибка миграции аватарки пользователя ID=" + avatar.getUserId() + ": " + e.getMessage());
            }
        }


        Map<String, Object> result = new HashMap<>();
        result.put("total", avatars.size());
        result.put("success", successCount);
        result.put("errors", errorCount);
        result.put("skipped", skippedCount);
        return result;
    }
}