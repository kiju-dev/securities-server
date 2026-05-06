package com.securities.securities_server.global.auth;

import com.securities.securities_server.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;

import static com.securities.securities_server.global.exception.ErrorCode.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthUserArgumentResolverTest {

    final AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

    @Test
    void AuthUser_어노테이션과_파라미터_타입이_Long이면_true를_반환한다() throws NoSuchMethodException {
        // given
        Method method = TestController.class.getMethod("successTest", Long.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void AuthUser_어노테이션과_파라미터_타입이_Long이_아니면_false를_반환한다() throws NoSuchMethodException {
        // given
        Method method = TestController.class.getMethod("failTypeTest", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void AuthUser_어노테이션이_없으면_false를_반환한다() throws NoSuchMethodException {
        // given
        Method method = TestController.class.getMethod("failAnnotationTest", Long.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void request에서_userId를_찾아_반환한다() throws Exception {
        // given
        Method method = TestController.class.getMethod("successTest", Long.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 1L);
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when
        Object result = resolver.resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(result).isEqualTo(1L);
    }

    @Test
    void request에_userId가_없으면_UNAUTHORIZED_예외가_발생한다() throws Exception {
        // given
        Method method = TestController.class.getMethod("successTest", Long.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        HttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, webRequest, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(UNAUTHORIZED);
    }

    static class TestController {
        public void successTest(@AuthUser Long userId) {
        }

        public void failTypeTest(@AuthUser String userId) {
        }

        public void failAnnotationTest(Long userId) {
        }
    }
}