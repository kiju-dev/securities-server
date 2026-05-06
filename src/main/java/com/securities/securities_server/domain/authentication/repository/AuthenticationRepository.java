package com.securities.securities_server.domain.authentication.repository;

import com.securities.securities_server.domain.authentication.entity.Authentication;
import com.securities.securities_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationRepository extends JpaRepository<Authentication, Long> {
    Optional<Authentication> findByUser(User user);

    Optional<Authentication> findByRefreshToken(String refreshToken);
}
