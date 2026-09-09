package com.project.main.controller.admin;

import com.project.main.controller.error.GlobalExceptionHandler;
import com.project.main.exception.NotFoundException;
import com.project.main.service.auth.SessionService;
import com.project.main.service.cases.CaseService;
import com.project.main.service.cases.SolutionService;
import com.project.main.service.component.ControllerHelperService;
import com.project.main.service.component.UserValidationService;
import com.project.main.service.learning.QuizService;
import com.project.main.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminApiControllerTest {

    @Mock private CaseService caseService;
    @Mock private UserService userService;
    @Mock private SolutionService solutionService;
    @Mock private QuizService quizService;
    @Mock private SessionService sessionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ControllerHelperService helper =
                new ControllerHelperService(new UserValidationService());
        AdminApiController controller = new AdminApiController(
                caseService, userService, solutionService, quizService, helper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(sessionService))
                .build();
    }

    @Test
    void attachTagToCaseReturnsSuccess() throws Exception {
        mockMvc.perform(post("/api/admin/v1/cases/1/tags/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(caseService).attachTagToCase(1L, 2L);
    }

    @Test
    void attachTagToCaseReturns404WhenTagMissing() throws Exception {
        doThrow(new NotFoundException("Tag not found"))
                .when(caseService).attachTagToCase(1L, 99L);

        mockMvc.perform(post("/api/admin/v1/cases/1/tags/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorText").value("Tag not found"));
    }
}