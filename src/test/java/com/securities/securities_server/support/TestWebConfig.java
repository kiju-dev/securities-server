package com.securities.securities_server.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@TestConfiguration
public class TestWebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new TestAuthUserArgumentResolver(1L));
    }
}
