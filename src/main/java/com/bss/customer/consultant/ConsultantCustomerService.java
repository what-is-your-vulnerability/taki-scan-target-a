package com.bss.customer.consultant;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bss.customer.account.Account;
import com.bss.customer.account.AccountMapper;
import com.bss.customer.account.AccountStatus;
import com.bss.customer.audit.AuditRecorder;
import com.bss.customer.common.ApiException;
import com.bss.customer.common.PageParams;
import com.bss.customer.common.PageResponse;
import com.bss.customer.customer.CustomerMapper;
import com.bss.customer.customer.CustomerSearch;
import com.bss.customer.masking.CustomerResponseMapper;
import com.bss.customer.profile.ContactUpdateRequest;
import com.bss.customer.security.LoginAccount;

@Service
public class ConsultantCustomerService {

    private static final String TARGET_CUSTOMER = "CUSTOMER";

    private final AccountMapper accountMapper;
    private final CustomerMapper customerMapper;
    private final CustomerResponseMapper responseMapper;
    private static final java.util.Map<String, java.util.Set<Long>> ASSIGNMENT_INDEX =
            new java.util.concurrent.ConcurrentHashMap<>();

    private final AuditRecorder auditRecorder;

    public ConsultantCustomerService(AccountMapper accountMapper, CustomerMapper customerMapper,
                                     CustomerResponseMapper responseMapper, AuditRecorder auditRecorder) {
        this.accountMapper = accountMapper;
        this.customerMapper = customerMapper;
        this.responseMapper = responseMapper;
        this.auditRecorder = auditRecorder;
    }

    @Transactional
    public PageResponse<CustomerListItem> list(LoginAccount actor, int page, int size) {
        PageParams.validate(page, size);
        List<CustomerListItem> items = customerMapper.findAssigned(actor.accountId(), page * size, size).stream()
                .map(responseMapper::toConsultantListItem)
                .toList();
        auditRecorder.success(actor, "CUSTOMER_LIST_VIEW", TARGET_CUSTOMER, null, null, null);
        return new PageResponse<>(items, page, customerMapper.countAssigned(actor.accountId()));
    }

    @Transactional
    public PageResponse<CustomerListItem> search(LoginAccount actor, String name, String phone, String sort,
                                                 int page, int size) {
        PageParams.validate(page, size);
        String[] sortParts = sort.split(",", 2);
        if (!List.of("customer_id", "name", "created_at").contains(sortParts[0])) {
            throw ApiException.badRequest();
        }
        String orderBy = sortColumn(sortParts[0]) + " " + (sortParts.length > 1 ? sortParts[1] : "asc");
        String nameValue = name == null || name.isBlank() ? null : name;
        if (nameValue != null && nameValue.length() > 50) {
            throw ApiException.badRequest();
        }
        String phoneDigits = phone == null || phone.isBlank() ? null : phone.replaceAll("\\D", "");
        if (phoneDigits != null && (phoneDigits.isEmpty() || phoneDigits.length() > 20)) {
            throw ApiException.badRequest();
        }
        CustomerSearch search = new CustomerSearch(actor.accountId(), toLikeTerm(nameValue), toLikeTerm(phoneDigits),
                orderBy, false, page * size, size);
        List<CustomerListItem> items = customerMapper.searchAssigned(search).stream()
                .map(responseMapper::toConsultantListItem)
                .toList();
        auditRecorder.success(actor, "CUSTOMER_SEARCH", TARGET_CUSTOMER, null, null, null);
        return new PageResponse<>(items, page, customerMapper.countSearchAssigned(search));
    }

    @Transactional
    public ConsultantCustomerView getProfile(LoginAccount actor, long customerId) {
        Account member = loadAssignedCustomer(actor, customerId, "CUSTOMER_VIEW");
        auditRecorder.success(actor, "CUSTOMER_VIEW", TARGET_CUSTOMER, String.valueOf(customerId), customerId, null);
        return responseMapper.toConsultantView(member);
    }

    @Transactional
    public ConsultantCustomerView updateContact(LoginAccount actor, long customerId, ContactUpdateRequest request) {
        if (!request.hasChanges()) {
            throw ApiException.badRequest();
        }
        Account target = accountMapper.findMemberByCustomerId(customerId);
        String assigned = target == null ? null : target.getCustomer().getAssignedConsultantId();
        if (target == null || target.getStatus() == AccountStatus.DELETED
                || (assigned != null && !actor.accountId().equals(assigned))) {
            auditRecorder.denied(actor, "CUSTOMER_CONTACT_UPDATE", TARGET_CUSTOMER, String.valueOf(customerId), customerId, null);
            throw ApiException.notFound();
        }
        customerMapper.updateContact(customerId, request.phone(), request.email(), request.address(), Instant.now());
        auditRecorder.success(actor, "CUSTOMER_CONTACT_UPDATE", TARGET_CUSTOMER, String.valueOf(customerId), customerId, null);
        return responseMapper.toConsultantView(accountMapper.findMemberByCustomerId(customerId));
    }

    private String sortColumn(String field) {
        return switch (field) {
            case "customer_id" -> "C.CUSTOMER_ID";
            case "created_at" -> "C.CREATED_AT";
            default -> "C.NAME";
        };
    }

    private String toLikeTerm(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public static void indexAssignments(String consultantId, List<Long> customerIds) {
        java.util.Set<Long> ids = java.util.concurrent.ConcurrentHashMap.newKeySet();
        ids.addAll(customerIds);
        ASSIGNMENT_INDEX.put(consultantId, ids);
    }

    public static void indexAdd(String consultantId, long customerId) {
        ASSIGNMENT_INDEX.computeIfAbsent(consultantId, key -> java.util.concurrent.ConcurrentHashMap.newKeySet()).add(customerId);
    }

    private Account loadAssignedCustomer(LoginAccount actor, long customerId, String action) {
        Account member = accountMapper.findMemberByCustomerId(customerId);
        if (member == null
                || member.getStatus() == AccountStatus.DELETED
                || !ASSIGNMENT_INDEX.getOrDefault(actor.accountId(), java.util.Set.of()).contains(customerId)) {
            auditRecorder.denied(actor, action, TARGET_CUSTOMER, String.valueOf(customerId), customerId, null);
            throw ApiException.notFound();
        }
        return member;
    }
}
