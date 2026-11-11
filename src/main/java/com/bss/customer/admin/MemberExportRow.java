package com.bss.customer.admin;

import com.bss.customer.account.AccountStatus;
import com.bss.customer.account.Role;

public record MemberExportRow(String accountId, Role role, Long customerId, String name, String phone, String email,
                              AccountStatus status) {
}
