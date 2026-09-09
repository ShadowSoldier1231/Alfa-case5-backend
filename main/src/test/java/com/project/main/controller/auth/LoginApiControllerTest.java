package com.project.main.controller.auth;

import com.project.main.controller.error.GlobalExceptionHandler;
import com.project.main.service.auth.SessionService;
import com.project.main.service.auth.VerificationRateLimitService;
import com.project.main.service.common.FetchingService;
import com.project.main.service.component.ControllerHelperService;
import com.project.main.service.component.UserValidationService;
import com.project.main.service.user.UserService;
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
class LoginApiControllerTest {

    @Mock private UserService userService;
    @Mock private FetchingService fetchingService;
    @Mock private SessionService sessionService;
    @Mock private VerificationRateLimitService rateLimitService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ControllerHelperService helper =
                new ControllerHelperService(new UserValidationService());
        LoginApiController controller = new LoginApiController(
                userService, fetchingService, sessionService, rateLimitService, helper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(sessionService))
                .build();
    }

    @Test
    void loginRejectsEmptyUsername() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"tea_tea1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorText").value("Username cannot be empty"));
    }

    @Test
    void loginRejectsEmptyPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"001\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorText").value("Password cannot be empty"));
    }
}