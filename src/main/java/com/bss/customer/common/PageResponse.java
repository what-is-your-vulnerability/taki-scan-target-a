package com.bss.customer.common;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, long total) {
}
