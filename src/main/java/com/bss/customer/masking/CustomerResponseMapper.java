package com.bss.customer.masking;

import org.springframework.stereotype.Component;

import com.bss.customer.account.Account;
import com.bss.customer.admin.MemberDetail;
import com.bss.customer.admin.MemberExportRow;
import com.bss.customer.admin.MemberListItem;
import com.bss.customer.consultant.ConsultantCustomerView;
import com.bss.customer.consultant.CustomerListItem;
import com.bss.customer.crypto.FieldEncryptor;
import com.bss.customer.customer.Customer;
import com.bss.customer.profile.MyProfile;

@Component
public class CustomerResponseMapper {

    private final FieldEncryptor fieldEncryptor;

    public CustomerResponseMapper(FieldEncryptor fieldEncryptor) {
        this.fieldEncryptor = fieldEncryptor;
    }

    public MyProfile toMyProfile(Account member) {
        Customer customer = member.getCustomer();
        return new MyProfile(
                customer.getCustomerId(),
                member.getLoginId(),
                customer.getName(),
                customer.getBirthDate(),
                Masking.residentRegNo(fieldEncryptor.decrypt(customer.getResidentRegNo())),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                member.getStatus(),
                customer.getUpdatedAt());
    }

    public CustomerListItem toConsultantListItem(Account member) {
        Customer customer = member.getCustomer();
        return new CustomerListItem(
                customer.getCustomerId(),
                Masking.name(customer.getName()),
                Masking.phone(customer.getPhone()),
                member.getStatus());
    }

    public ConsultantCustomerView toConsultantView(Account member) {
        Customer customer = member.getCustomer();
        return new ConsultantCustomerView(
                customer.getCustomerId(),
                Masking.name(customer.getName()),
                Masking.birthDate(customer.getBirthDate()),
                Masking.phone(customer.getPhone()),
                Masking.email(customer.getEmail()),
                Masking.address(customer.getAddress()),
                customer.getCreditStatus(),
                customer.getCollectionStatus(),
                member.getStatus());
    }

    public MemberListItem toMemberListItem(Account member) {
        Customer customer = member.getCustomer();
        return new MemberListItem(
                member.getAccountId(),
                member.getRole(),
                member.getCustomerId(),
                customer == null ? member.getStaffName() : customer.getName(),
                customer == null ? null : customer.getPhone(),
                member.getStatus(),
                customer == null ? null : customer.getResidentRegNo(),
                customer == null ? null : customer.getEmail());
    }

    public MemberExportRow toMemberExportRow(Account member) {
        Customer customer = member.getCustomer();
        return new MemberExportRow(
                member.getAccountId(),
                member.getRole(),
                member.getCustomerId(),
                customer == null ? member.getStaffName() : customer.getName(),
                customer == null ? null : Masking.phone(customer.getPhone()),
                customer == null ? null : customer.getEmail(),
                member.getStatus());
    }

    public MemberExportRow toMemberExportSpoolRow(Account member) {
        Customer customer = member.getCustomer();
        return new MemberExportRow(
                member.getAccountId(),
                member.getRole(),
                member.getCustomerId(),
                customer == null ? member.getStaffName() : customer.getName(),
                customer == null ? null : customer.getPhone(),
                customer == null ? null : customer.getEmail(),
                member.getStatus());
    }

    public MemberDetail toMemberDetail(Account member) {
        Customer customer = member.getCustomer();
        MemberDetail.CustomerPart customerPart = customer == null ? null : new MemberDetail.CustomerPart(
                customer.getCustomerId(),
                customer.getName(),
                String.valueOf(customer.getBirthDate()),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getCreditStatus(),
                customer.getCollectionStatus(),
                customer.getAssignedConsultantId(),
                customer.getCreatedAt(),
                customer.getResidentRegNo());
        return new MemberDetail(
                member.getAccountId(),
                member.getLoginId(),
                member.getRole(),
                member.getStatus(),
                member.getFailedLoginCount(),
                member.getIdentityVerifiedAt(),
                member.getLastLoginAt(),
                member.getStaffName(),
                customerPart);
    }
}
