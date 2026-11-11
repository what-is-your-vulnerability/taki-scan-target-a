package com.bss.customer.admin;

import java.time.Instant;

import com.bss.customer.account.AccountStatus;
import com.bss.customer.account.Role;

public record MemberDetail(
        String accountId,
        String loginId,
        Role role,
        AccountStatus status,
        int failedLoginCount,
        Instant identityVerifiedAt,
        Instant lastLoginAt,
        String staffName,
        CustomerPart customer) {

    public record CustomerPart(
            Long customerId,
            String name,
            String birthDate,
            String phone,
            String email,
            String address,
            String creditStatus,
            String collectionStatus,
            String assignedConsultantId,
            Instant createdAt,
            String residentRegNo) {
    }
}
