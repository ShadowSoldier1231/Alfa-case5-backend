package com.project.main.service.component;

import com.project.main.enums.ValidPasswordStatus;
import com.project.main.enums.ValidUsernameStatus;
import com.project.main.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.stream.Collectors;

@Service
public class ControllerHelperService {

    private final UserValidationService validationService;

    public ControllerHelperService(UserValidationService validationService) {
        this.validationService = validationService;
    }

    public String getValidationErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }

    public void validateBindingResult(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }
    }

    public void validatePassword(String password) {
        ValidPasswordStatus status = validationService.checkPassword(password);

        switch (status) {
            case EMPTY:
                throw new BadRequestException("Password cannot be empty");
            case TOO_LONG:
                throw new BadRequestException("Password cannot be longer than 30 characters");
            case TOO_SHORT:
                throw new BadRequestException("Password cannot be shorter than 8 characters");
            case NO_DIGITS:
                throw new BadRequestException("Password must contain at least 1 digit");
            case NO_SPECIAL_SYMBOL:
                throw new BadRequestException("Password must contain at least 1 special character");
            case OK:
            default:
                break;
        }
    }

    public void validateUsername(String username) {
        ValidUsernameStatus status = validationService.checkUsername(username);

        switch (status) {
            case TOO_LONG:
                throw new BadRequestException("Username cannot be longer than 20 characters");
            case TOO_SHORT:
                throw new BadRequestException("Username cannot be shorter than 3 characters");
            case EMPTY:
                throw new BadRequestException("Username cannot be empty");
            case SPACE:
                throw new BadRequestException("Username cannot contain spaces");
            case OK:
            default:
                break;
        }
    }
}