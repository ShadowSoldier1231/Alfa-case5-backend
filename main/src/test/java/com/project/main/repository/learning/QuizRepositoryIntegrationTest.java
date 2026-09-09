package com.project.main.repository.learning;

import com.project.main.enums.Difficulty;
import com.project.main.model.cases.CaseEntity;
import com.project.main.model.learning.Quiz;
import com.project.main.model.learning.StudyMaterial;
import com.project.main.repository.cases.CaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class QuizRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private StudyMaterialRepository materialRepository;

    @Autowired
    private QuizRepository quizRepository;

    private Quiz quizOverMaterial(boolean materialActive) {
        CaseEntity caseEntity = caseRepository.saveAndFlush(new CaseEntity(
                "quiz-case", "Кейс с тестом", "Quiz case", "desc", "full",
                Difficulty.MEDIUM, 30, null, null, null, true, 0, null));
        StudyMaterial material = materialRepository.saveAndFlush(
                new StudyMaterial(caseEntity.getId(), "text", "title", 1, materialActive));
        return quizRepository.saveAndFlush(new Quiz(material.getId(), "Quiz"));
    }

    @Test
    void activeQuizIsFoundWhenCaseAndMaterialAreActive() {
        Quiz quiz = quizOverMaterial(true);

        assertThat(quizRepository.findActiveQuizWithActiveMaterialById(quiz.getId())).isPresent();
        assertThat(quizRepository.findActiveByMaterialId(quiz.getMaterialId())).hasSize(1);
    }

    @Test
    void quizIsNotFoundWhenMaterialIsInactive() {
        Quiz quiz = quizOverMaterial(false);

        assertThat(quizRepository.findActiveQuizWithActiveMaterialById(quiz.getId())).isEmpty();
    }
}