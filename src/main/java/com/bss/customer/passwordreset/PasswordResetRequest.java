package com.bss.customer.passwordreset;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(@JsonProperty(value = "login_id", required = true) @NotBlank @Size(max = 30) String loginId) {
}
