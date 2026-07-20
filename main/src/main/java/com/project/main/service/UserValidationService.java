package com.project.main.service;


import com.project.main.enums.ValidPasswordStatus;
import com.project.main.enums.ValidUsernameStatus;
import org.springframework.stereotype.Service;

@Service
public class UserValidationService {

    public ValidPasswordStatus checkPassword(String password) {
        if (password == null || password.isBlank()) return ValidPasswordStatus.EMPTY;
        if (password.length() < 8) return ValidPasswordStatus.TOO_SHORT;
        if (password.length() > 30) return ValidPasswordStatus.TOO_LONG;

        String spec = "!@#$%^&*()_-+=;:/?|\\<>{}[]";
        boolean hasSpec = password.chars().anyMatch(ch -> spec.indexOf(ch) >= 0);
        if (!hasSpec) return ValidPasswordStatus.NO_SPECIAL_SYMBOL;

        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasDigit) return ValidPasswordStatus.NO_DIGITS;

        return ValidPasswordStatus.OK;
    }

    public ValidUsernameStatus checkUsername(String username) {
        if (username == null || username.isBlank()) return ValidUsernameStatus.EMPTY;
        if (username.length() < 3) return ValidUsernameStatus.TOO_SHORT;
        if (username.length() > 20) return ValidUsernameStatus.TOO_LONG;
        if (username.contains(" ")) return ValidUsernameStatus.SPACE;

        return ValidUsernameStatus.OK;
    }
}
