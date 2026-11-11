package com.bss.customer.consultant;

import com.bss.customer.account.AccountStatus;

public record CustomerListItem(Long customerId, String name, String phone, AccountStatus status) {
}
