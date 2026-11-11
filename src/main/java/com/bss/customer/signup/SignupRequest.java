package com.bss.customer.signup;

import java.time.LocalDate;

import com.bss.customer.profile.ContactRules;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9._-]{4,30}$") String loginId,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 50) String name,
        @NotNull LocalDate birthDate,
        @NotBlank @Pattern(regexp = "^\\d{6}-?\\d{7}$") String residentRegNo,
        @NotBlank @Size(max = 20) @Pattern(regexp = ContactRules.PHONE) String phone,
        @NotBlank @Size(max = 100) @Pattern(regexp = ContactRules.EMAIL) String email,
        @NotBlank @Size(max = 200) String address,
        String role,
        String status,
        String assignedConsultantId,
        String creditStatus) {
}
