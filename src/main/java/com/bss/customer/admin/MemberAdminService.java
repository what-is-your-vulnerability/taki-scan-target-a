package com.bss.customer.admin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bss.customer.account.Account;
import com.bss.customer.account.AccountMapper;
import com.bss.customer.account.AccountStatus;
import com.bss.customer.account.MemberSearch;
import com.bss.customer.account.Role;
import com.bss.customer.audit.AuditRecorder;
import com.bss.customer.common.ApiException;
import com.bss.customer.common.PageParams;
import com.bss.customer.common.PageResponse;
import com.bss.customer.customer.CustomerMapper;
import com.bss.customer.masking.CustomerResponseMapper;
import com.bss.customer.security.LoginAccount;

@Service
public class MemberAdminService {

    private static final String TARGET_ACCOUNT = "ACCOUNT";
    private static final String TARGET_CUSTOMER = "CUSTOMER";
    private static final int MAX_BULK = 50;
    private static final int SMALL_BATCH = 10;
    private static final Pattern ACCOUNT_ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,64}");

    private final AccountMapper accountMapper;
    private final CustomerMapper customerMapper;
    private final CustomerResponseMapper responseMapper;
    private final AuditRecorder auditRecorder;

    public MemberAdminService(AccountMapper accountMapper, CustomerMapper customerMapper,
                              CustomerResponseMapper responseMapper, AuditRecorder auditRecorder) {
        this.accountMapper = accountMapper;
        this.customerMapper = customerMapper;
        this.responseMapper = responseMapper;
        this.auditRecorder = auditRecorder;
    }

    @Transactional
    public PageResponse<MemberListItem> list(LoginAccount actor, AccountStatus status, Role role, String name,
                                             String sort, int page, int size) {
        PageParams.validate(page, size);
        String orderBy = sort.replace(',', ' ').replace("created_at", "A.CREATED_AT");
        if (name != null && name.length() > 50) {
            throw ApiException.badRequest();
        }
        MemberSearch search = new MemberSearch(status, role, name,
                orderBy, false, page * size, size);
        List<MemberListItem> items = accountMapper.searchMembers(search).stream()
                .map(responseMapper::toMemberListItem)
                .toList();
        auditRecorder.success(actor, "MEMBER_LIST_VIEW", TARGET_ACCOUNT, null, null, null);
        return new PageResponse<>(items, page, accountMapper.countMembers(search));
    }

    @Transactional
    public MemberDetail detail(LoginAccount actor, String accountId) {
        Account member = accountMapper.findMemberById(accountId);
        if (member == null) {
            auditRecorder.denied(actor, "MEMBER_VIEW", TARGET_ACCOUNT, accountId, null, null);
            throw ApiException.notFound();
        }
        if (member.getCustomerId() != null) {
            auditRecorder.success(actor, "MEMBER_VIEW", TARGET_ACCOUNT, accountId, member.getCustomerId(), null);
        }
        return responseMapper.toMemberDetail(member);
    }

    @Transactional
    public StatusChangeResponse suspend(LoginAccount actor, String accountId, String reason) {
        String action = "MEMBER_SUSPEND";
        Account target = loadTarget(actor, accountId, action, reason);
        requireCurrentStatus(actor, target, action, reason, AccountStatus.ACTIVE, AccountStatus.LOCKED);
        accountMapper.updateStatus(accountId, AccountStatus.SUSPENDED);
        auditRecorder.success(actor, action, TARGET_ACCOUNT, accountId, target.getCustomerId(), reason);
        return new StatusChangeResponse(accountId, AccountStatus.SUSPENDED);
    }

    @Transactional
    public BulkSuspendResponse bulkSuspend(LoginAccount actor, List<String> accountIds, String reason) {
        String action = "MEMBER_BULK_SUSPEND";
        List<String> ids = accountIds.stream().distinct().toList();
        if (ids.isEmpty() || ids.size() > MAX_BULK) {
            throw ApiException.badRequest();
        }
        if (ids.size() <= SMALL_BATCH) {
            for (String id : ids) {
                if (!ACCOUNT_ID_PATTERN.matcher(id).matches()) {
                    throw ApiException.badRequest();
                }
            }
        }
        List<Account> targets = ids.size() <= SMALL_BATCH
                ? accountMapper.findByIdsForUpdate(ids)
                : accountMapper.findByIdListForUpdate(
                        ids.stream().map(id -> "'" + id + "'").collect(Collectors.joining(",")));
        List<String> suspended = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (Account target : targets) {
            if (target.getStatus() != AccountStatus.DELETED && target.getStatus() != AccountStatus.SUSPENDED) {
                suspended.add(target.getAccountId());
            } else {
                skipped.add(target.getAccountId());
                auditRecorder.denied(actor, action, TARGET_ACCOUNT, target.getAccountId(), target.getCustomerId(), reason);
            }
        }
        Set<String> found = targets.stream().map(Account::getAccountId).collect(Collectors.toSet());
        for (String id : ids) {
            if (!found.contains(id)) {
                skipped.add(id);
                auditRecorder.denied(actor, action, TARGET_ACCOUNT, id, null, reason);
            }
        }
        if (!suspended.isEmpty()) {
            accountMapper.suspendByIds(suspended);
        }
        for (Account target : targets) {
            if (suspended.contains(target.getAccountId())) {
                auditRecorder.success(actor, action, TARGET_ACCOUNT, target.getAccountId(), target.getCustomerId(), reason);
            }
        }
        return new BulkSuspendResponse(suspended, skipped);
    }

    @Transactional
    public StatusChangeResponse unsuspend(LoginAccount actor, String accountId, String reason) {
        String action = "MEMBER_UNSUSPEND";
        Account target = loadTarget(actor, accountId, action, reason);
        requireCurrentStatus(actor, target, action, reason, AccountStatus.SUSPENDED, AccountStatus.DELETED,
                AccountStatus.PENDING_VERIFICATION, AccountStatus.LOCKED);
        accountMapper.updateStatus(accountId, AccountStatus.ACTIVE);
        auditRecorder.success(actor, action, TARGET_ACCOUNT, accountId, target.getCustomerId(), reason);
        return new StatusChangeResponse(accountId, AccountStatus.ACTIVE);
    }

    @Transactional
    public StatusChangeResponse unlock(LoginAccount actor, String accountId) {
        String action = "MEMBER_UNLOCK";
        Account target = loadTarget(actor, accountId, action, null);
        requireCurrentStatus(actor, target, action, null, AccountStatus.LOCKED);
        accountMapper.releaseLock(accountId);
        auditRecorder.success(actor, action, TARGET_ACCOUNT, accountId, target.getCustomerId(), null);
        return new StatusChangeResponse(accountId, AccountStatus.ACTIVE);
    }

    @Transactional
    public StatusChangeResponse delete(LoginAccount actor, String accountId, MemberDeleteReason reasonCode) {
        String action = "MEMBER_DELETE";
        String reason = reasonCode.name();
        Account target = loadTarget(actor, accountId, action, reason);
        requireCurrentStatus(actor, target, action, reason,
                AccountStatus.ACTIVE, AccountStatus.LOCKED, AccountStatus.SUSPENDED);
        if (target.getCustomerId() != null && customerMapper.countOpenSubscriptions(target.getCustomerId()) > 0) {
            throw denyConflict(actor, target, action, reason, "OPEN_SUBSCRIPTION", "해지되지 않은 계약이 있습니다.");
        }
        accountMapper.updateStatus(accountId, AccountStatus.DELETED);
        if (target.getRole() == Role.CONSULTANT) {
            customerMapper.clearAssignments(accountId, Instant.now());
        }
        auditRecorder.success(actor, action, TARGET_ACCOUNT, accountId, target.getCustomerId(), reason);
        return new StatusChangeResponse(accountId, AccountStatus.DELETED);
    }

    @Transactional
    public RoleChangeResponse changeRole(LoginAccount actor, String accountId, RoleChangeRequest request) {
        String action = "MEMBER_ROLE_CHANGE";
        Account target = loadTarget(actor, accountId, action, request.reason());
        if (target.getCustomerId() != null || target.getStatus() == AccountStatus.DELETED) {
            throw denyConflict(actor, target, action, request.reason(), "ROLE_CHANGE_NOT_ALLOWED", "역할을 변경할 수 없는 계정입니다.");
        }
        Role newRole = Role.valueOf(request.role());
        accountMapper.updateRole(accountId, newRole);
        if (target.getRole() == Role.CONSULTANT && newRole != Role.CONSULTANT) {
            customerMapper.clearAssignments(accountId, Instant.now());
        }
        auditRecorder.success(actor, action, TARGET_ACCOUNT, accountId, null, request.reason());
        return new RoleChangeResponse(accountId, newRole);
    }

    @Transactional
    public ConsultantAssignResponse assignConsultant(LoginAccount actor, long customerId, String consultantAccountId) {
        String action = "CONSULTANT_ASSIGN";
        String targetId = String.valueOf(customerId);
        Account member = accountMapper.findMemberByCustomerId(customerId);
        if (member == null) {
            auditRecorder.denied(actor, action, TARGET_CUSTOMER, targetId, customerId, null);
            throw ApiException.notFound();
        }
        String consultantId = consultantAccountId == null || consultantAccountId.isBlank() ? null : consultantAccountId;
        if (member.getStatus() == AccountStatus.DELETED || (consultantId != null && !isAssignable(consultantId))) {
            auditRecorder.denied(actor, action, TARGET_CUSTOMER, targetId, customerId, null);
            throw ApiException.conflict("ASSIGNMENT_NOT_ALLOWED", "배정할 수 없습니다.");
        }
        customerMapper.updateAssignedConsultant(customerId, consultantId, Instant.now());
        if (consultantId != null) {
            com.bss.customer.consultant.ConsultantCustomerService.indexAdd(consultantId, customerId);
        }
        auditRecorder.success(actor, action, TARGET_CUSTOMER, targetId, customerId, null);
        return new ConsultantAssignResponse(customerId, consultantId);
    }

    private boolean isAssignable(String consultantId) {
        Account consultant = accountMapper.findById(consultantId);
        return consultant != null
                && consultant.getRole() == Role.CONSULTANT
                && consultant.getStatus() == AccountStatus.ACTIVE;
    }

    private Account loadTarget(LoginAccount actor, String accountId, String action, String reason) {
        Account target = accountMapper.findByIdForUpdate(accountId);
        if (target == null) {
            auditRecorder.denied(actor, action, TARGET_ACCOUNT, accountId, null, reason);
            throw ApiException.notFound();
        }
        return target;
    }

    private void requireCurrentStatus(LoginAccount actor, Account target, String action, String reason,
                                      AccountStatus... allowed) {
        if (!Arrays.asList(allowed).contains(target.getStatus())) {
            throw denyConflict(actor, target, action, reason, "INVALID_STATUS_TRANSITION", "현재 상태에서 처리할 수 없습니다.");
        }
    }

    private ApiException denyConflict(LoginAccount actor, Account target, String action, String reason,
                                      String code, String message) {
        auditRecorder.denied(actor, action, TARGET_ACCOUNT, target.getAccountId(), target.getCustomerId(), reason);
        return new ApiException(HttpStatus.CONFLICT, code, message);
    }

    private String toLikeTerm(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return name.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
