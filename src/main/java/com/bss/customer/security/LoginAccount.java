package com.bss.customer.security;

import com.bss.customer.account.Role;

public record LoginAccount(String accountId, Long customerId, Role role) {
}
