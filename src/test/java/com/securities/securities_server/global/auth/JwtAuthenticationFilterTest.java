package com.securities.securities_server.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securities.securities_server.global.exception.CustomException;
import com.securities.securities_server.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JwtAuthenticationFilterTest {

    @Mock
    JwtProvider jwtProvider;

    @Mock
    FilterChain filterChain;

    JwtAuthenticationFilter filter;
    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filter = new JwtAuthenticationFilter(jwtProvider, new ObjectMapper());
    }

    @Test
    void 유효한_AccessToken이면_userId를_request에_저장한다() throws ServletException, IOException {
        // given
        String token = "header.payload.signature";
        request.addHeader("Authorization", "Bearer " + token);
        given(jwtProvider.getUserId(token)).willReturn(1L);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(request.getAttribute("userId")).isEqualTo(1L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void 유효하지_않은_AccessToken이면_INVALID_TOKEN_응답을_반환한다() throws Exception {
        // given
        String token = "invalid.token";
        request.addHeader("Authorization", "Bearer " + token);

        doThrow(new CustomException(ErrorCode.INVALID_TOKEN))
                .when(jwtProvider)
                .validate(token);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void 만료된_AccessToken이면_EXPIRED_TOKEN_응답을_반환한다() throws Exception {
        // given
        String token = "expired.token";
        request.addHeader("Authorization", "Bearer " + token);

        doThrow(new CustomException(ErrorCode.EXPIRED_TOKEN))
                .when(jwtProvider)
                .validate(token);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void Authorization_헤더가_존재하지_않는다면_그냥_통과한다() throws ServletException, IOException {
        // given
        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(request .getAttribute("userId")).isNull();
        verify(jwtProvider, never()).validate(anyString());
        verify(filterChain).doFilter(request, response);
    }
}