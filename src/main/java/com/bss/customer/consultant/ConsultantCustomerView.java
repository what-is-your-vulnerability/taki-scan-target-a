package com.bss.customer.consultant;

import com.bss.customer.account.AccountStatus;

public record ConsultantCustomerView(
        Long customerId,
        String name,
        String birthDate,
        String phone,
        String email,
        String address,
        String creditStatus,
        String collectionStatus,
        AccountStatus status) {
}
