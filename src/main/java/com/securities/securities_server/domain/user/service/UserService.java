package com.securities.securities_server.domain.user.service;

import com.securities.securities_server.domain.user.controller.request.SignUpRequest;
import com.securities.securities_server.domain.user.controller.response.SignUpResponse;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.domain.user.repository.UserRepository;
import com.securities.securities_server.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.securities.securities_server.global.exception.ErrorCode.DUPLICATE_EMAIL;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(DUPLICATE_EMAIL);
        }
    }
}
