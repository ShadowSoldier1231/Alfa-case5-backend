package com.project.main.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionStatusTest {

    @Test
    void apiExceptionsCarryExpectedHttpStatuses() {
        assertThat(new BadRequestException("x").getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(new NotFoundException("x").getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new ConflictException("x").getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(new TooManyRequestsException("x").getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(new InvalidCredentialsException("x").getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(new InternalServerErrorException("x").getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void invalidSessionExceptionKeepsTokenForCookieCleanup() {
        InvalidSessionException ex = new InvalidSessionException("Session expired", "abc");
        assertThat(ex.getToken()).isEqualTo("abc");
        assertThat(ex.getMessage()).isEqualTo("Session expired");
    }
}