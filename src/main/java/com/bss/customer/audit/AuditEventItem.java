package com.bss.customer.audit;

import java.time.Instant;

public record AuditEventItem(long eventId, String actorId, String action, String targetType, String targetId,
                             Long customerId, String result, Instant occurredAt, String reason) {
}
