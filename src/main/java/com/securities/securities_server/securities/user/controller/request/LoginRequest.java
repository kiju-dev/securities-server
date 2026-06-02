package com.securities.securities_server.securities.user.controller.request;

public record LoginRequest(
        String email,
        String password
) {
}
