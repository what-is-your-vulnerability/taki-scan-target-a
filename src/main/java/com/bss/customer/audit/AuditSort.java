package com.bss.customer.audit;

import java.util.Arrays;

import com.bss.customer.common.ApiException;

public enum AuditSort {
    OCCURRED_AT_ASC("occurred_at,asc", "occurred_at", false),
    OCCURRED_AT_DESC("occurred_at,desc", "occurred_at", true),
    ACTION_ASC("action,asc", "action", false),
    ACTION_DESC("action,desc", "action", true),
    RESULT_ASC("result,asc", "result", false),
    RESULT_DESC("result,desc", "result", true);

    private final String value;
    private final String field;
    private final boolean descending;

    AuditSort(String value, String field, boolean descending) {
        this.value = value;
        this.field = field;
        this.descending = descending;
    }

    public static AuditSort parse(String value) {
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
