package com.project.main.service.auth;

import com.project.main.exception.InvalidSessionException;
import com.project.main.model.user.UserSession;
import com.project.main.repository.user.UserSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock private UserSessionRepository sessionRepository;

    private SessionService service() {
        return new SessionService(sessionRepository);
    }

    @Test
    void getUserIdOrThrowRejectsMissingToken() {
        assertThatThrownBy(() -> service().getUserIdOrThrow(null))
                .isInstanceOf(InvalidSessionException.class)
                .hasMessage("Please login first");
    }

    @Test
    void getUserIdOrThrowRejectsExpiredSession() {
        UserSession session = new UserSession();
        session.setUserId(5L);
        session.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(sessionRepository.findByToken("old")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service().getUserIdOrThrow("old"))
                .isInstanceOf(InvalidSessionException.class)
                .hasMessage("Session expired");
    }

    @Test
    void getUserIdOrThrowReturnsUserIdForValidSession() {
        UserSession session = new UserSession();
        session.setUserId(5L);
        session.setExpiryDate(LocalDateTime.now().plusHours(1));
        when(sessionRepository.findByToken("ok")).thenReturn(Optional.of(session));

        assertThat(service().getUserIdOrThrow("ok")).isEqualTo(5L);
    }
}