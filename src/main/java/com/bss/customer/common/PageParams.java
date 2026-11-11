package com.bss.customer.common;

public final class PageParams {

    private static final int MAX_PAGE = 1_000_000;

    private PageParams() {
    }

    public static void validate(int page, int size) {
        if (page < 0 || page > MAX_PAGE || size < 1 || size > 100) {
            throw ApiException.badRequest();
        }
    }
}
