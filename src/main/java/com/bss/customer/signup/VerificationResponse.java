package com.bss.customer.signup;

import java.time.Instant;

public record VerificationResponse(String verificationId, String redirectUrl, Instant expiresAt) {
}
