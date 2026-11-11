package com.bss.customer.signup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificationCompleteRequest(@NotBlank @Size(max = 200) String agencyResultRef) {
}
