package com.bss.customer.auth;

import com.bss.customer.account.Role;

public record LoginResponse(String accountId, Role role) {
}
