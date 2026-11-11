package com.bss.customer.admin;

import com.bss.customer.account.AccountStatus;

public record StatusChangeResponse(String accountId, AccountStatus status) {
}
