package com.bss.customer.support;

import com.bss.customer.account.AccountStatus;
import com.bss.customer.account.Role;

public record MemberLookupItem(String accountId, Role role, Long customerId, String name, AccountStatus status) {
}
