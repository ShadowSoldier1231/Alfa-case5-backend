package com.project.main.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project.main.dto.RegisterResult;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RegisterResult> handleJsonMappingException(HttpMessageNotReadableException ex) {

        String errorMsg = "Invalid input data format";


        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {

            if (invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) {


                String fieldName = invalidFormatException.getPath().isEmpty()
                        ? "field"
                        : invalidFormatException.getPath().getFirst().getFieldName();


                errorMsg = switch (fieldName) {
                    case "validationMethod" -> "Invalid validation method provided";
                    default -> "Invalid value provided for field: " + fieldName;
                };
            }
        }


        return ResponseEntity.ok(new RegisterResult(false, errorMsg));
    }
}