package com.securities.securities_server.domain.Authentication.repository;

import com.securities.securities_server.domain.Authentication.entity.Authentication;
import com.securities.securities_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationRepository extends JpaRepository<Authentication, Long> {
    Optional<Authentication> findByUser(User user);
}
