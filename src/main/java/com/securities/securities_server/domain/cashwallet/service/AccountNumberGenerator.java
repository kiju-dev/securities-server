package com.securities.securities_server.domain.cashwallet.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String PREFIX = "777";
    private static final String SUFFIX = "1";

    public String generateAccountNumber() {
        StringBuilder sb = new StringBuilder();

        sb.append(PREFIX);
        for (int i = 0; i < 8; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        sb.append(SUFFIX);

        return sb.toString();
    }
}
