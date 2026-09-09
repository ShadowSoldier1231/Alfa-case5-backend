package com.project.main.controller.integration;

import com.project.main.controller.error.GlobalExceptionHandler;
import com.project.main.service.auth.SessionService;
import com.project.main.service.cases.CaseService;
import com.project.main.service.cases.SolutionService;
import com.project.main.service.component.MicroserviceValidationComponent;
import com.project.main.service.user.UserModerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TextAnalysisIntegrationControllerTest {

    @Mock private UserModerationService moderationService;
    @Mock private SolutionService solutionService;
    @Mock private CaseService caseService;
    @Mock private SessionService sessionService;
    @Mock private MicroserviceValidationComponent microserviceValidator;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TextAnalysisIntegrationController controller = new TextAnalysisIntegrationController(
                moderationService, solutionService, caseService, sessionService, microserviceValidator);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(sessionService))
                .build();
    }

    @Test
    void addScoreRejectsRatingAbove100() throws Exception {
        mockMvc.perform(post("/api/text/v1/addScore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseId\":1,\"rating\":101,\"solutionText\":\"a\",\"solutionResponse\":\"b\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorText").value("Invalid rating value"));
    }

    @Test
    void addScoreRejectsBlankSolutionText() throws Exception {
        mockMvc.perform(post("/api/text/v1/addScore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseId\":1,\"rating\":50,\"solutionText\":\"  \",\"solutionResponse\":\"b\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorText").value("Invalid request"));
    }
}