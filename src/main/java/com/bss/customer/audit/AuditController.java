package com.bss.customer.audit;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bss.customer.common.PageResponse;
import com.bss.customer.security.LoginAccount;

@RestController
@RequestMapping("/api/v1/admin")
public class AuditController {

    private final AuditQueryService auditQueryService;

    public AuditController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping("/audit-events")
    @PreAuthorize("!hasRole('CUSTOMER')")
    public PageResponse<AuditEventItem> list(@AuthenticationPrincipal LoginAccount actor,
                                             @RequestParam(name = "actor_id", required = false) String actorId,
                                             @RequestParam(required = false) String action,
                                             @RequestParam(required = false) String from,
                                             @RequestParam(required = false) String to,
                                             @RequestParam(defaultValue = "occurred_at,desc") String sort,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return auditQueryService.search(actor, actorId, action, from, to, sort, page, size);
    }
}
