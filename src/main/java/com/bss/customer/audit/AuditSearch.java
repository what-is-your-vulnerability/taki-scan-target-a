package com.bss.customer.audit;

import java.time.LocalDate;

public record AuditSearch(String actorId, String action, String from, String to,
                          String sortField, boolean descending, int offset, int size) {
}
