package com.securities.securities_server.domain.user.service;

import com.securities.securities_server.domain.user.controller.request.LoginRequest;
import com.securities.securities_server.domain.user.controller.request.SignUpRequest;
import com.securities.securities_server.domain.user.controller.response.LoginResponse;
import com.securities.securities_server.domain.user.controller.response.SignUpResponse;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.domain.user.repository.UserRepository;
import com.securities.securities_server.global.auth.JwtProvider;
import com.securities.securities_server.global.auth.TokenInfo;
import com.securities.securities_server.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.securities.securities_server.global.exception.ErrorCode.DUPLICATE_EMAIL;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_CREDENTIAL;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        validateDuplicateEmail(request.email());
        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.signUp(
                request.name(),
                request.email(),
                encodedPassword
        );
        User savedUser = userRepository.save(user);
        return new SignUpResponse(savedUser.getId());
    }

    public LoginResponse login(LoginRequest request) {
        User user = getUserForLogin(request.email());
        verifyPassword(request.password(), user.getPassword());
        TokenInfo tokenInfo = jwtProvider.createAccessToken(user.getId());

        return LoginResponse.of(tokenInfo);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(DUPLICATE_EMAIL);
        }
    }

    private User getUserForLogin(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(INVALID_CREDENTIAL));
    }

    private void verifyPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new CustomException(INVALID_CREDENTIAL);
        }
    }
}
