package com.securities.securities_server.global.exception;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.securities.securities_server.global.exception.ErrorCode.DUPLICATE_EMAIL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@Import({
        GlobalExceptionHandler.class,
        GlobalExceptionHandlerTest.TestController.class
})
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void CustomException_발생_시_해당하는_ErrorCode를_반환한다() throws Exception {
        mockMvc.perform(get("/custom-exception"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("USER_001"))
                .andExpect(jsonPath("$.message").value("해당 이메일로 가입된 회원이 이미 있습니다."));
    }

    @Test
    void 예상하지_못한_Exception_발생_시_INTERNAL_SERVER_ERROR로_반환한다() throws Exception {
        mockMvc.perform(get("/exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("SERVER_001"))
                .andExpect(jsonPath("$.message").value("서버 내부에 오류가 발생했습니다."));
    }

    @RestController
    static class TestController {

        @GetMapping("/custom-exception")
        public void throwCustomException() {
            throw new CustomException(DUPLICATE_EMAIL);
        }

        @GetMapping("/exception")
        public void throwException() {
            throw new RuntimeException("예상치 못한 에러 발생");
        }
    }
}