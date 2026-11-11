package com.bss.customer.account;

public record MemberSearch(AccountStatus status, Role role, String nameLike,
                           String sortField, boolean descending, int offset, int size) {
}
