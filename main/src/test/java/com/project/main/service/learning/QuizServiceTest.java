package com.project.main.service.learning;

import com.project.main.dto.learning.QuizSubmitRequest;
import com.project.main.exception.BadRequestException;
import com.project.main.model.learning.Quiz;
import com.project.main.repository.learning.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock private StudyMaterialRepository materialRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private AnswerOptionRepository answerOptionRepository;
    @Mock private QuizAttemptRepository attemptRepository;
    @Mock private UserAnswerRepository userAnswerRepository;

    private QuizService service() {
        return new QuizService(materialRepository, quizRepository, questionRepository,
                answerOptionRepository, attemptRepository, userAnswerRepository);
    }

    @Test
    void submitQuizAttemptRejectsInvalidQuizId() {
        assertThatThrownBy(() -> service().submitQuizAttempt(1L, 0L, new QuizSubmitRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid quiz ID");
    }

    @Test
    void submitQuizAttemptRejectsEmptyAnswers() {
        when(quizRepository.findActiveQuizWithActiveMaterialById(4L))
                .thenReturn(Optional.of(new Quiz(1L, "Quiz")));

        assertThatThrownBy(() -> service().submitQuizAttempt(1L, 4L, new QuizSubmitRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Answers cannot be empty");
    }

    @Test
    void submitQuizAttemptRejectsDuplicateQuestionIds() {
        when(quizRepository.findActiveQuizWithActiveMaterialById(4L))
                .thenReturn(Optional.of(new Quiz(1L, "Quiz")));

        QuizSubmitRequest request = new QuizSubmitRequest();
        QuizSubmitRequest.UserAnswerDto first = new QuizSubmitRequest.UserAnswerDto();
        first.setQuestionId(41L);
        first.setAnswerOptionId(101L);
        QuizSubmitRequest.UserAnswerDto second = new QuizSubmitRequest.UserAnswerDto();
        second.setQuestionId(41L); // дубликат того же вопроса
        second.setAnswerOptionId(102L);
        request.setAnswers(List.of(first, second));

        assertThatThrownBy(() -> service().submitQuizAttempt(1L, 4L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Duplicate question ID in request");
    }
}