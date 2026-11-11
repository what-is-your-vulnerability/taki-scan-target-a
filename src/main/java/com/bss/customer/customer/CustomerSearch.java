package com.bss.customer.customer;

public record CustomerSearch(String consultantId, String nameLike, String phoneLike,
                             String sortField, boolean descending, int offset, int size) {
}
