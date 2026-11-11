package com.bss.customer.audit;

import java.time.Instant;

public class AuditEvent {

    private long eventId;
    private String actorId;
    private String action;
    private String targetType;
    private String targetId;
    private Long customerId;
    private String reason;
    private Instant occurredAt;
    private String result;

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}
