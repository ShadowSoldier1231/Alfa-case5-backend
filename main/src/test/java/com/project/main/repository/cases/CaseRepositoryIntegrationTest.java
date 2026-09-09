package com.project.main.repository.cases;

import com.project.main.enums.Difficulty;
import com.project.main.model.cases.CaseEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class CaseRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private EntityManager entityManager;

    private CaseEntity savedCase() {
        return caseRepository.save(new CaseEntity(
                "demo-case", "Демо кейс", "Demo case", "desc", "full desc",
                Difficulty.EASY, 60, null, null, null, true, 0, null));
    }

    @Test
    void savedCaseIsFoundByActiveCheckAndPublicSearch() {
        CaseEntity saved = savedCase();

        assertThat(caseRepository.existsActiveCaseById(saved.getId())).isTrue();

        Page<CaseEntity> page = caseRepository.findPublicCases("демо", PageRequest.of(0, 25));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getSlug()).isEqualTo("demo-case");
    }

    @Test
    void incrementViewsCountReallyUpdatesCounter() {
        CaseEntity saved = savedCase();
        entityManager.flush();

        caseRepository.incrementViewsCount(saved.getId());
        entityManager.clear(); // сбрасываем кеш

        CaseEntity reloaded = caseRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getViewsCount()).isEqualTo(1);
    }
}