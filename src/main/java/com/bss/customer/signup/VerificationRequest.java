package com.bss.customer.signup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificationRequest(@NotBlank @Size(max = 64) String signupRef) {
}
