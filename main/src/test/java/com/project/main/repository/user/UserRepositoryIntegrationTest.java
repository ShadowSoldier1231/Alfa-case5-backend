package com.project.main.repository.user;

import com.project.main.enums.UserRole;
import com.project.main.model.user.UserSession;
import com.project.main.model.user.UserSetup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository sessionRepository;

    @Test
    void savedUserIsFoundByUsernameAndEmail() {
        userRepository.saveAndFlush(new UserSetup(
                "hash", "tester", "tester@mail.ru", UserRole.USER, null, true));

        assertThat(userRepository.findByUsername("tester")).isPresent();
        assertThat(userRepository.existsByEmail("tester@mail.ru")).isTrue();
    }

    @Test
    void savedSessionIsFoundByTokenWithUserId() {
        UserSession session = new UserSession();
        session.setToken("t-1");
        session.setUserId(42L);
        session.setExpiryDate(LocalDateTime.now().plusHours(1));
        sessionRepository.saveAndFlush(session);

        Optional<UserSession> found = sessionRepository.findByToken("t-1");

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(42L);
    }
}