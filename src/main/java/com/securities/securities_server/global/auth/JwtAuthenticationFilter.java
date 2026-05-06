package com.securities.securities_server.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securities.securities_server.global.exception.CustomException;
import com.securities.securities_server.global.exception.ErrorCode;
import com.securities.securities_server.global.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());

            try {
                jwtProvider.validate(token);
                Long userId = jwtProvider.getUserId(token);
                request.setAttribute("userId", userId);
            } catch (CustomException e) {
                ErrorCode errorCode = e.getErrorCode();
                ErrorResponse errorResponse = ErrorResponse.from(errorCode);
                response.setStatus(errorResponse.status());
                response.setContentType("application/json;charset=UTF-8");
                objectMapper.writeValue(response.getWriter(), errorResponse);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
