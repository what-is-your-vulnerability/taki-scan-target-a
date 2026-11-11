package com.bss.customer.admin;

import java.util.Arrays;

import com.bss.customer.common.ApiException;

public enum MemberSort {
    CREATED_AT_ASC("created_at,asc", "created_at", false),
    CREATED_AT_DESC("created_at,desc", "created_at", true),
    NAME_ASC("name,asc", "name", false),
    NAME_DESC("name,desc", "name", true),
    STATUS_ASC("status,asc", "status", false),
    STATUS_DESC("status,desc", "status", true);

    private final String value;
    private final String field;
    private final boolean descending;

    MemberSort(String value, String field, boolean descending) {
        this.value = value;
        this.field = field;
        this.descending = descending;
    }

    public static MemberSort parse(String value) {
        return Arrays.stream(values())
                .filter(sort -> sort.value.equals(value))
                .findFirst()
                .orElseThrow(ApiException::badRequest);
    }

    public String field() {
        return field;
    }

    public boolean descending() {
        return descending;
    }
}
