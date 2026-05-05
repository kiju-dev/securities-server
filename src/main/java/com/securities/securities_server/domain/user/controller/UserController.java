package com.securities.securities_server.domain.user.controller;

import com.securities.securities_server.domain.user.controller.request.LoginRequest;
import com.securities.securities_server.domain.user.controller.request.SignUpRequest;
import com.securities.securities_server.domain.user.controller.response.LoginResponse;
import com.securities.securities_server.domain.user.controller.response.SignUpResponse;
import com.securities.securities_server.domain.user.controller.response.TokenResponse;
import com.securities.securities_server.domain.user.service.UserService;
import com.securities.securities_server.global.cookie.CookieProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CookieProvider cookieProvider;

    @PostMapping("/api/v1/user")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = userService.signUp(request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<LoginResponse> login(HttpServletResponse response, @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = userService.login(request);
        LoginResponse loginResponse = LoginResponse.of(tokenResponse.accessTokenInfo());
        cookieProvider.addRefreshTokenCookie(response, tokenResponse.refreshTokenInfo().refreshToken());

        return ResponseEntity.status(OK).body(loginResponse);
    }
}
