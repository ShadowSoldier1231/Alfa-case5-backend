package com.project.main.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project.main.dto.RegisterResult;
import com.project.main.exception.ApiException;
import com.project.main.exception.InvalidSessionException;
import com.project.main.model.Views;
import com.project.main.service.SessionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private final SessionService sessionService;

    public GlobalExceptionHandler(SessionService sessionService){
        this.sessionService = sessionService;
    }

    @JsonView(Views.RegisterResultPartial.class)
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


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RegisterResult(false, errorMsg));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<RegisterResult> handleNotFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RegisterResult(false, "Resource not found"));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RegisterResult> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new RegisterResult(false, "Method not allowed"));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RegisterResult> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RegisterResult(false, "Missing required parameter: " + ex.getParameterName()));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RegisterResult> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(new RegisterResult(false, "File size exceeds the maximum allowed limit"));
    }


    @JsonView(Views.RegisterResultPartial.class)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<RegisterResult> handleApiException(ApiException ex) {
        RegisterResult error = new RegisterResult();
        error.setSuccess(false);
        error.setErrorText(ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @JsonView(Views.RegisterResultPartial.class)
    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<RegisterResult> handleInvalidSessionException(InvalidSessionException ex,
                                                                        HttpServletResponse response) {
        RegisterResult error = new RegisterResult();
        error.setSuccess(false);
        error.setErrorText(ex.getMessage());
        ResponseCookie cookie = sessionService.deleteCookie(ex.getToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

}