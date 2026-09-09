package com.project.main.controller.web;

import com.project.main.controller.error.GlobalExceptionHandler;
import com.project.main.dto.cases.CasePublicDto;
import com.project.main.enums.Difficulty;
import com.project.main.exception.NotFoundException;
import com.project.main.service.auth.SessionService;
import com.project.main.service.cases.CaseService;
import com.project.main.service.learning.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CaseControllerTest {

    @Mock private CaseService caseService;
    @Mock private SessionService sessionService;
    @Mock private QuizService quizService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CaseController controller = new CaseController(caseService, sessionService, quizService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(sessionService))
                .build();
    }

    @Test
    void getCaseByIdReturnsCaseJson() throws Exception {
        when(caseService.getCaseByIdAndIncrementViews(1L)).thenReturn(sampleCase());

        mockMvc.perform(get("/api/v1/cases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.slug").value("demo-case"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.caseRating").value(4.5));
    }

    @Test
    void getCaseByIdReturns404WhenCaseNotFound() throws Exception {
        when(caseService.getCaseByIdAndIncrementViews(99L))
                .thenThrow(new NotFoundException("Case not found"));

        mockMvc.perform(get("/api/v1/cases/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorText").value("Case not found"));
    }

    private CasePublicDto sampleCase() {
        return new CasePublicDto(
                1L, "demo-case", "Demo case", "Demo case",
                "Short description", "Full description",
                Difficulty.EASY, 60, null, null, 10,
                LocalDateTime.of(2026, 1, 1, 12, 0),
                LocalDateTime.of(2026, 1, 2, 12, 0),
                4.5, List.of()
        );
    }
}