package com.securities.securities_server.domain.user.service;

import com.securities.securities_server.domain.Authentication.repository.AuthenticationRepository;
import com.securities.securities_server.domain.user.controller.request.LoginRequest;
import com.securities.securities_server.domain.user.controller.request.SignUpRequest;
import com.securities.securities_server.domain.user.controller.response.SignUpResponse;
import com.securities.securities_server.domain.user.controller.response.TokenResponse;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.domain.user.repository.UserRepository;
import com.securities.securities_server.global.auth.AccessTokenInfo;
import com.securities.securities_server.global.auth.JwtProvider;
import com.securities.securities_server.global.auth.RefreshTokenInfo;
import com.securities.securities_server.global.exception.CustomException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static com.securities.securities_server.global.exception.ErrorCode.DUPLICATE_EMAIL;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_CREDENTIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    AuthenticationRepository authenticationRepository;

    @Mock
    PasswordEncoder passwordEncoder;
    
    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    UserService userService;

    @Test
    void 회원가입에_성공한다() {
        // given
        SignUpRequest request = new SignUpRequest(
                "kiju", "kiju@gmail.com", "Password12!@"
        );
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        User savedUser = User.signUp(
                request.name(), request.email(), request.password()
        );
        given(userRepository.save(any())).willReturn(savedUser);

        // when
        SignUpResponse response = userService.signUp(request);

        // then
        assertThat(response.userId()).isEqualTo(savedUser.getId());
        verify(userRepository).save(any());
    }


    @Test
    void 이미_가입된_이메일이_존재하면_DUPLICATE_EMAIL_예외를_반환한다() {
        // given
        SignUpRequest request = new SignUpRequest(
                "kiju", "kiju@gmail.com", "Password12!@"
        );
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DUPLICATE_EMAIL);
    }

    @Test
    void 이메일과_비밀번호를_사용하여_로그인에_성공한다() {
        // given
        User user = createUser();
        LoginRequest request = new LoginRequest("kiju@gmail.com", "Password12!@");
        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);

        Instant accessTokenexpiredAt = Instant.parse("2026-05-05T00:00:00Z");
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo("accessToken", accessTokenexpiredAt);
        given(jwtProvider.createAccessToken(user.getId())).willReturn(accessTokenInfo);

        Instant refreshTokenexpiredAt = Instant.parse("2026-05-12T00:00:00Z");
        RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo("refreshToken", refreshTokenexpiredAt);
        given(jwtProvider.createRefreshToken(user.getId())).willReturn(refreshTokenInfo);

        // when
        TokenResponse response = userService.login(request);

        // then
        assertThat(response.accessTokenInfo().accessToken())
                .isEqualTo(accessTokenInfo.accessToken());
        assertThat(response.accessTokenInfo().accessTokenExpiredAt())
                .isEqualTo(accessTokenInfo.accessTokenExpiredAt());
        assertThat(response.refreshTokenInfo().refreshToken())
                .isEqualTo(refreshTokenInfo.refreshToken());
        assertThat(response.refreshTokenInfo().refreshTokenExpiredAt())
                .isEqualTo(refreshTokenInfo.refreshTokenExpiredAt());
        verify(authenticationRepository).save(any());
    }

    @Test
    void 이메일이_존재하지_않으면_INVALID_CREDENTIAL_예외가_발생한다() {
        // given
        LoginRequest request = new LoginRequest("unknown@gmail.com", "Password12!@");
        given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_CREDENTIAL);
    }

    @Test
    void 비밀번호가_올바르지_않으면_INVALID_CREDENTIAL_예외가_발생한다() {
        // given
        User user = createUser();
        LoginRequest request = new LoginRequest("kiju@gmail.com", "wrongPassword");
        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_CREDENTIAL);
    }

    private User createUser() {
        return User.signUp("kiju", "kiju@gmail.com", "encodedPassword");
    }
    
}