package com.bss.customer.passwordreset;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @JsonProperty(value = "token", required = true) @NotBlank @Size(max = 200) String token,
        @JsonProperty(value = "new_password", required = true) @NotBlank @Size(min = 8, max = 72) String newPassword) {
}
