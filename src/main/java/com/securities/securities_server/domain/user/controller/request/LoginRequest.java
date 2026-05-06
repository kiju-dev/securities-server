package com.securities.securities_server.domain.user.controller.request;

public record LoginRequest(
        String email,
        String password
) {
}
