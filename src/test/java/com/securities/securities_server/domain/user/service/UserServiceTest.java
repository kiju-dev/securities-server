package com.securities.securities_server.domain.user.service;

import com.securities.securities_server.domain.user.controller.request.SignUpRequest;
import com.securities.securities_server.domain.user.controller.response.SignUpResponse;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.domain.user.repository.UserRepository;
import com.securities.securities_server.global.exception.CustomException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.securities.securities_server.global.exception.ErrorCode.DUPLICATE_EMAIL;
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
    PasswordEncoder passwordEncoder;

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


}