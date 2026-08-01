package com.smartbank.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class AccountNumberGenerator {

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}