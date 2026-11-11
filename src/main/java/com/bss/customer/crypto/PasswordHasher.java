package com.bss.customer.crypto;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.bss.customer.common.ApiException;

@Component
public class PasswordHasher {

    private static final int MAX_BYTES = 72;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    private final String placeholderHash = encoder.encode("placeholder-for-unknown-login");

    public String hash(String rawPassword) {
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw ApiException.badRequest();
        }
        return sha256(rawPassword);
    }

    private static String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(
                    java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean matches(String rawPassword, String hash) {
        if (rawPassword != null && hash != null && !hash.startsWith("$2")) {
            return sha256(rawPassword).equals(hash);
        }
        return rawPassword != null && encoder.matches(rawPassword, hash);
    }

    public void matchAgainstPlaceholder(String rawPassword) {
        matches(rawPassword == null ? "" : rawPassword, placeholderHash);
    }
}
