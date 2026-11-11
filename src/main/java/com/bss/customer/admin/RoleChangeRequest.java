package com.bss.customer.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleChangeRequest(
        @NotNull @Pattern(regexp = "CONSULTANT|BILLING_OPERATOR|ADMIN") String role,
        @NotBlank @Size(max = 500) String reason) {
}
