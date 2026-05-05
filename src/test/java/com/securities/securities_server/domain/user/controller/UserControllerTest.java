package com.securities.securities_server.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securities.securities_server.domain.user.controller.request.SignUpRequest;
import com.securities.securities_server.domain.user.controller.response.SignUpResponse;
import com.securities.securities_server.domain.user.service.UserService;
import com.securities.securities_server.global.auth.JwtProvider;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    void 회원가입_성공_시_userId를_반환한다() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("kiju", "kiju@gmail.com", "Password12!@");
        given(userService.signUp(any())).willReturn(new SignUpResponse(1L));

        // when & then
        mockMvc.perform(post("/api/v1/user")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Nested
    class 이름은 {

        @ParameterizedTest
        @ValueSource(ints = {2, 4, 6, 8, 10})
        void 길이가_2이상_10이하여야_한다(int length) throws Exception {
            // given
            String name = "a".repeat(length);
            SignUpRequest request = new SignUpRequest(name, "kiju@gmail.com", "Password12!@");

            // when & then
            mockMvc.perform(post("/api/v1/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 11, 20})
        void 길이가_1이하_11이상이면_예외가_발생한다(int length) throws Exception {
            // given
            String name = "a".repeat(length);
            SignUpRequest request = new SignUpRequest(name, "kiju@gmail.com", "Password12!@");

            // when & then
            mockMvc.perform(post("/api/v1/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class 이메일은 {

        @ParameterizedTest
        @ValueSource(strings = {
                "a@b.comcom",
                "kiju@gmail.com",
                "test123@gmail.com"
        })
        void 길이가_10이상_100이하여야_한다(String email) throws Exception {
            // given
            SignUpRequest request = new SignUpRequest("kiju", email, "Password12!@");
            // when & then
            mockMvc.perform(post("/api/v1/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                " ",
                "abc",
                "kiju",
        })
        void 이메일_형식이_아니면_예외가_발생한다(String email) throws Exception {
            // given
            SignUpRequest request = new SignUpRequest("kiju", email, "Password12!@");

            // when & then
            mockMvc.perform(post("/api/v1/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(ints = {9, 101})
        void 길이가_10미만_100초과면_예외가_발생한다(int length) throws Exception {
            // given
            String email = "a".repeat(length);
            SignUpRequest request = new SignUpRequest("kiju", email, "Password12!@");

            // when & then
            mockMvc.perform(post("/api/v1/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class 비밀번호는 {

        @ParameterizedTest
        @ValueSource(strings = {
                "Password1!",
                "Password12!@",
                "Abcdef12!@"
        })
        void 길이가_8이상_32이하여야_하고_대소문자_특수문자_숫자를_포함해야_한다(String password) throws Exception {
            // given
            SignUpRequest request = new SignUpRequest("kiju", "kiju@gmail.com", password);

            // when & then
            mockMvc.perform(post("/api/v1/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                " ",
                "Pass1!",
                "password12!",
                "PASSWORD12!",
                "Password!!",
                "Password12",
                "Password12%"
        })
        void 조건을_만족하지_않으면_예외가_발생한다(String password) throws Exception {
            // given
            SignUpRequest request = new SignUpRequest("kiju", "kiju@gmail.com", password);

            // when & then
            mockMvc.perform(post("/api/v1/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 길이가_32글자를_초과하면_예외가_발생한다() throws Exception {
            // given
            String password = "Password12!" + "a".repeat(23);
            SignUpRequest request = new SignUpRequest("kiju", "kiju@gmail.com", password);

            // when & then
            mockMvc.perform(post("/api/v1/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}