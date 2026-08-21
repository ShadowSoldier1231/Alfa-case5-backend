package com.project.main.filter;

import com.project.main.exception.InvalidSessionException;
import com.project.main.model.user.UserSession;
import com.project.main.model.user.UserSetup;
import com.project.main.repository.user.UserRepository;
import com.project.main.repository.user.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Component
public class SessionFilter extends OncePerRequestFilter {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Autowired
    public SessionFilter(UserSessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {


        String token = (request.getCookies() == null) ? null :
                Arrays.stream(request.getCookies())
                        .filter(c -> "token".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst().orElse(null);

        if (token != null) {
            Optional<UserSetup> userOpt = sessionRepository.findByToken(token)
                    .filter(s -> s.getExpiryDate().isAfter(LocalDateTime.now()))
                    .map(UserSession::getUserId)
                    .flatMap(userRepository::findById);

            if (userOpt.isPresent()) {
                UserSetup user = userOpt.get();

                if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    throw new InvalidSessionException("User is still banned", token);
                }

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }


            filterChain.doFilter(request, response);
        }
    }
}