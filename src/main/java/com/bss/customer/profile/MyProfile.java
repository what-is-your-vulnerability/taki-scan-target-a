package com.bss.customer.profile;

import java.time.Instant;
import java.time.LocalDate;

import com.bss.customer.account.AccountStatus;

public record MyProfile(
        Long customerId,
        String loginId,
        String name,
        LocalDate birthDate,
        String residentRegNo,
        String phone,
        String email,
        String address,
        AccountStatus status,
        Instant updatedAt) {
}
