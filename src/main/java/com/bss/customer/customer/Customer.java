package com.bss.customer.customer;

import java.time.Instant;
import java.time.LocalDate;

public class Customer {

    private Long customerId;
    private String name;
    private LocalDate birthDate;
    private String residentRegNo;
    private String phone;
    private String email;
    private String address;
    private String creditStatus;
    private String collectionStatus;
    private String assignedConsultantId;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getResidentRegNo() { return residentRegNo; }
    public void setResidentRegNo(String residentRegNo) { this.residentRegNo = residentRegNo; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCreditStatus() { return creditStatus; }
    public void setCreditStatus(String creditStatus) { this.creditStatus = creditStatus; }
    public String getCollectionStatus() { return collectionStatus; }
    public void setCollectionStatus(String collectionStatus) { this.collectionStatus = collectionStatus; }
    public String getAssignedConsultantId() { return assignedConsultantId; }
    public void setAssignedConsultantId(String assignedConsultantId) { this.assignedConsultantId = assignedConsultantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
