package com.bss.customer.account;

import java.time.Instant;

import com.bss.customer.customer.Customer;

public class Account {

    private String accountId;
    private String loginId;
    private Long customerId;
    private String staffName;
    private String signupRef;
    private String passwordHash;
    private Role role;
    private AccountStatus status;
    private int failedLoginCount;
    private int sessionEpoch;
    private Instant identityVerifiedAt;
    private Instant passwordChangedAt;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Customer customer;

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public String getSignupRef() { return signupRef; }
    public void setSignupRef(String signupRef) { this.signupRef = signupRef; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public int getFailedLoginCount() { return failedLoginCount; }
    public void setFailedLoginCount(int failedLoginCount) { this.failedLoginCount = failedLoginCount; }
    public int getSessionEpoch() { return sessionEpoch; }
    public void setSessionEpoch(int sessionEpoch) { this.sessionEpoch = sessionEpoch; }
    public Instant getIdentityVerifiedAt() { return identityVerifiedAt; }
    public void setIdentityVerifiedAt(Instant identityVerifiedAt) { this.identityVerifiedAt = identityVerifiedAt; }
    public Instant getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(Instant passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}
