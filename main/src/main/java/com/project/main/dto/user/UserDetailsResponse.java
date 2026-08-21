package com.project.main.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserRole;
import com.project.main.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserDetailsResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String middleName,
        String lastName,
        String nickName,
        UserStatus status,
        GenderCode gender,

        @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDate birthdate,

        String cityName,
        String regionName,
        Long score,
        Long placement,
        String avatarUrl,

        UserRole role,

        @JsonProperty("isVerified")
        Boolean isVerified,

        LocalDateTime bannedUntil,
        LocalDateTime creationDate
) {}