package com.bss.customer.signup;

import java.time.Instant;

public class IdentityVerification {

    private String verificationId;
    private String accountId;
    private String agencyResultRef;
    private String status;
    private Instant expiresAt;
    private Instant completedAt;
    private Instant createdAt;

    public String getVerificationId() { return verificationId; }
    public void setVerificationId(String verificationId) { this.verificationId = verificationId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getAgencyResultRef() { return agencyResultRef; }
    public void setAgencyResultRef(String agencyResultRef) { this.agencyResultRef = agencyResultRef; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
