package com.bss.customer.admin;

import com.bss.customer.account.Role;

public record RoleChangeResponse(String accountId, Role role) {
}
