package com.project.main.controller.error;

import com.project.main.dto.common.RegisterResult;
import com.project.main.exception.NotFoundException;
import com.project.main.service.auth.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler(mock(SessionService.class));

    @Test
    void apiExceptionIsMappedToItsOwnStatusAndMessage() {
        ResponseEntity<RegisterResult> response =
                handler.handleApiException(new NotFoundException("Case not found"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getSuccess()).isFalse();
        assertThat(response.getBody().getErrorText()).isEqualTo("Case not found");
    }

    @Test
    void unexpectedExceptionIsMappedTo500WithGenericMessage() {
        ResponseEntity<RegisterResult> response =
                handler.handleGeneric(new IllegalStateException("boom"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getErrorText()).isEqualTo("Internal server error");
    }
}