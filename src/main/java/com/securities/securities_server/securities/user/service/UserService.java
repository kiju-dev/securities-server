package com.securities.securities_server.securities.user.service;

import com.securities.securities_server.securities.authentication.entity.Authentication;
import com.securities.securities_server.securities.authentication.repository.AuthenticationRepository;
import com.securities.securities_server.securities.user.controller.request.LoginRequest;
import com.securities.securities_server.securities.user.controller.request.SignUpRequest;
import com.securities.securities_server.securities.user.controller.response.SignUpResponse;
import com.securities.securities_server.securities.user.controller.response.TokenResponse;
import com.securities.securities_server.securities.user.entity.User;
import com.securities.securities_server.securities.user.repository.UserRepository;
import com.securities.securities_server.global.auth.AccessTokenInfo;
import com.securities.securities_server.global.auth.JwtProvider;
import com.securities.securities_server.global.auth.RefreshTokenInfo;
import com.securities.securities_server.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.securities.securities_server.global.exception.ErrorCode.DUPLICATE_EMAIL;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_CREDENTIAL;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_TOKEN;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationRepository authenticationRepository;
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

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = getUserForLogin(request.email());
        verifyPassword(request.password(), user.getPassword());
        AccessTokenInfo accessTokenInfo = jwtProvider.createAccessToken(user.getId());
        RefreshTokenInfo refreshTokenInfo = jwtProvider.createRefreshToken(user.getId());
        saveRefreshToken(user, refreshTokenInfo);

        return TokenResponse.of(accessTokenInfo, refreshTokenInfo);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        jwtProvider.validate(refreshToken);
        Authentication authentication = authenticationRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(INVALID_TOKEN));
        User user = authentication.getUser();

        AccessTokenInfo accessTokenInfo = jwtProvider.createAccessToken(user.getId());
        RefreshTokenInfo refreshTokenInfo = jwtProvider.createRefreshToken(user.getId());
        authentication.updateRefreshToken(
                refreshTokenInfo.refreshToken(),
                refreshTokenInfo.refreshTokenExpiredAt()
        );

        return TokenResponse.of(accessTokenInfo, refreshTokenInfo);
    }

    private void saveRefreshToken(User user, RefreshTokenInfo refreshTokenInfo) {
        authenticationRepository.findByUser(user)
                .ifPresentOrElse(
                        existing -> {
                            existing.updateRefreshToken(
                                    refreshTokenInfo.refreshToken(),
                                    refreshTokenInfo.refreshTokenExpiredAt()
                            );
                        },
                        () -> {
                            Authentication newAuthentication = new Authentication(
                                    user,
                                    refreshTokenInfo.refreshToken(),
                                    refreshTokenInfo.refreshTokenExpiredAt()
                            );
                            authenticationRepository.save(newAuthentication);
                        }
                );
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
