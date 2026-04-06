package com.vouchera.backend.util;

import java.util.Locale;

public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    public static String normalize(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}