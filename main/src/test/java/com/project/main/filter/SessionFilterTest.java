package com.project.main.filter;

import com.project.main.model.user.UserSession;
import com.project.main.model.user.UserSetup;
import com.project.main.repository.user.UserRepository;
import com.project.main.repository.user.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionFilterTest {

    @Mock private UserSessionRepository sessionRepository;
    @Mock private UserRepository userRepository;
    @Mock private FilterChain filterChain;

    @Test
    void requestWithoutCookiePassesThrough() throws Exception {
        SessionFilter filter = new SessionFilter(sessionRepository, userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void bannedUserGets403AndChainIsNotContinued() throws Exception {
        UserSession session = new UserSession();
        session.setToken("t");
        session.setUserId(1L);
        session.setExpiryDate(LocalDateTime.now().plusHours(1));

        UserSetup user = new UserSetup();
        user.setId(1L);
        user.setBannedUntil(LocalDateTime.now().plusDays(1));

        when(sessionRepository.findByToken("t")).thenReturn(Optional.of(session));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        SessionFilter filter = new SessionFilter(sessionRepository, userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("token", "t"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("User is still banned");
        verify(filterChain, never()).doFilter(request, response);
    }
}