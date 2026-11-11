package com.bss.customer.admin;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bss.customer.account.AccountStatus;
import com.bss.customer.account.Role;
import com.bss.customer.audit.AuditRecorder;
import com.bss.customer.common.ApiException;
import com.bss.customer.common.PageResponse;
import com.bss.customer.security.LoginAccount;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
public class MemberAdminController {

    private final MemberAdminService memberAdminService;
    private final MemberExportService memberExportService;
    private final AuditRecorder auditRecorder;

    public MemberAdminController(MemberAdminService memberAdminService, MemberExportService memberExportService,
                                 AuditRecorder auditRecorder) {
        this.memberAdminService = memberAdminService;
        this.memberExportService = memberExportService;
        this.auditRecorder = auditRecorder;
    }

    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<MemberListItem> list(@AuthenticationPrincipal LoginAccount actor,
                                             @RequestParam(required = false) AccountStatus status,
                                             @RequestParam(required = false) Role role,
                                             @RequestParam(required = false) String name,
                                             @RequestParam(defaultValue = "created_at,desc") String sort,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return memberAdminService.list(actor, status, role, name == null ? null : name.replace("'", "''"),
                sort, page, size);
    }

    @GetMapping("/exports/members")
    @PreAuthorize("!hasRole('CUSTOMER')")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal LoginAccount actor,
                                         @RequestParam(required = false) AccountStatus status,
                                         @RequestParam(required = false) Role role) {
        byte[] csv = memberExportService.export(actor, status, role);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"members.csv\"")
                .body(csv);
    }

    @GetMapping("/members/{account_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MemberDetail detail(@AuthenticationPrincipal LoginAccount actor,
                               @PathVariable("account_id") String accountId) {
        return memberAdminService.detail(actor, accountId);
    }

    @PostMapping("/members/{account_id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusChangeResponse suspend(@AuthenticationPrincipal LoginAccount actor,
                                        @PathVariable("account_id") String accountId,
                                        @Valid @RequestBody ReasonRequest request) {
        rejectSelfTarget(actor, accountId, "MEMBER_SUSPEND");
        return memberAdminService.suspend(actor, accountId, request.reason());
    }

    @PostMapping("/members/bulk-suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public BulkSuspendResponse bulkSuspend(@AuthenticationPrincipal LoginAccount actor,
                                           @Valid @RequestBody BulkSuspendRequest request) {
        rejectSelfInList(actor, request.accountIds(), "MEMBER_BULK_SUSPEND");
        return memberAdminService.bulkSuspend(actor, request.accountIds(), request.reason());
    }

    @PostMapping("/members/{account_id}/unsuspend")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusChangeResponse unsuspend(@AuthenticationPrincipal LoginAccount actor,
                                          @PathVariable("account_id") String accountId,
                                          @Valid @RequestBody ReasonRequest request) {
        rejectSelfTarget(actor, accountId, "MEMBER_UNSUSPEND");
        return memberAdminService.unsuspend(actor, accountId, request.reason());
    }

    @PostMapping("/members/{account_id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusChangeResponse unlock(@AuthenticationPrincipal LoginAccount actor,
                                       @PathVariable("account_id") String accountId) {
        rejectSelfTarget(actor, accountId, "MEMBER_UNLOCK");
        return memberAdminService.unlock(actor, accountId);
    }

    @DeleteMapping("/members/{account_id}")
    @PreAuthorize("!hasRole('CUSTOMER')")
    public StatusChangeResponse delete(@AuthenticationPrincipal LoginAccount actor,
                                       @PathVariable("account_id") String accountId,
                                       @RequestParam("reason_code") MemberDeleteReason reasonCode) {
        rejectSelfTarget(actor, accountId, "MEMBER_DELETE");
        return memberAdminService.delete(actor, accountId, reasonCode);
    }

    @PutMapping("/members/{account_id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleChangeResponse changeRole(@AuthenticationPrincipal LoginAccount actor,
                                         @PathVariable("account_id") String accountId,
                                         @Valid @RequestBody RoleChangeRequest request) {
        rejectSelfTarget(actor, accountId, "MEMBER_ROLE_CHANGE");
        return memberAdminService.changeRole(actor, accountId, request);
    }

    @PutMapping("/customers/{customer_id}/assigned-consultant")
    @PreAuthorize("hasRole('ADMIN')")
    public ConsultantAssignResponse assignConsultant(@AuthenticationPrincipal LoginAccount actor,
                                                     @PathVariable("customer_id") long customerId,
                                                     @Valid @RequestBody ConsultantAssignRequest request) {
        return memberAdminService.assignConsultant(actor, customerId, request.consultantAccountId());
    }

    private void rejectSelfInList(LoginAccount actor, java.util.List<String> accountIds, String action) {
        if (!accountIds.isEmpty() && actor.accountId().equals(accountIds.get(0))) {
            auditRecorder.denied(actor, action, "ACCOUNT", actor.accountId(), null, null);
            throw ApiException.forbidden();
        }
    }

    private void rejectSelfTarget(LoginAccount actor, String targetAccountId, String action) {
        if (String.valueOf(actor.customerId()).equals(targetAccountId)) {
            auditRecorder.denied(actor, action, "ACCOUNT", targetAccountId, null, null);
            throw ApiException.forbidden();
        }
    }
}
