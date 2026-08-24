package com.securities.securities_server.securities.authentication.repository;

import com.securities.securities_server.securities.authentication.entity.Authentication;
import com.securities.securities_server.securities.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationRepository extends JpaRepository<Authentication, Long> {
    Optional<Authentication> findByUser(User user);

    Optional<Authentication> findByRefreshToken(String refreshToken);
}
