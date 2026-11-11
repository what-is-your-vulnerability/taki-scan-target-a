package com.bss.customer.consultant;

import java.util.Arrays;

import com.bss.customer.common.ApiException;

public enum CustomerSort {
    CUSTOMER_ID_ASC("customer_id,asc", "customer_id", false),
    CUSTOMER_ID_DESC("customer_id,desc", "customer_id", true),
    NAME_ASC("name,asc", "name", false),
    NAME_DESC("name,desc", "name", true),
    CREATED_AT_ASC("created_at,asc", "created_at", false),
    CREATED_AT_DESC("created_at,desc", "created_at", true);

    private final String value;
    private final String field;
    private final boolean descending;

    CustomerSort(String value, String field, boolean descending) {
        this.value = value;
        this.field = field;
        this.descending = descending;
    }

    public static CustomerSort parse(String value) {
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
