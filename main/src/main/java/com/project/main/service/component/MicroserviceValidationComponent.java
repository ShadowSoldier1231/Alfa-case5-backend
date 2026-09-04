package com.project.main.service.component;

import com.project.main.exception.InvalidCredentialsException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class MicroserviceValidationComponent {

    private static final Logger logger = LoggerFactory.getLogger(MicroserviceValidationComponent.class);

    private final String serviceToken;
    private final String serviceHeader;

    public MicroserviceValidationComponent(
            @Value("${integration.ml.service-token}") String serviceToken,
            @Value("${integration.ml.service-header}") String serviceHeader
    ) {
        this.serviceToken = serviceToken;
        this.serviceHeader = serviceHeader;
    }

    @PostConstruct
    void warnIfNotConfigured() {
        if (!StringUtils.hasText(serviceHeader) || !StringUtils.hasText(serviceToken)) {
            logger.warn("Service header or token is not configured");
        }
    }

    public void validate(HttpServletRequest request) {
        if (!StringUtils.hasText(serviceHeader) || !StringUtils.hasText(serviceToken)) {
            logger.error("ML integration validation is not configured");
            throw new InvalidCredentialsException("Invalid integration credentials");
        }

        if (request == null) {
            throw new InvalidCredentialsException("Invalid integration credentials");
        }

        String headerValue = request.getHeader(serviceHeader);

        if (!StringUtils.hasText(headerValue)) {
            throw new InvalidCredentialsException("Invalid integration credentials");
        }

        if (!constantTimeEquals(headerValue, serviceToken)) {
            throw new InvalidCredentialsException("Invalid integration credentials");
        }
    }

    private boolean constantTimeEquals(String actual, String expected) {
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actualBytes, expectedBytes);
    }
}