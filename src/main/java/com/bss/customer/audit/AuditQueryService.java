package com.bss.customer.audit;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bss.customer.common.ApiException;
import com.bss.customer.common.PageParams;
import com.bss.customer.common.PageResponse;
import com.bss.customer.security.LoginAccount;

@Service
public class AuditQueryService {

    private final AuditMapper auditMapper;
    private final AuditRecorder auditRecorder;

    public AuditQueryService(AuditMapper auditMapper, AuditRecorder auditRecorder) {
        this.auditMapper = auditMapper;
        this.auditRecorder = auditRecorder;
    }

    @Transactional
    public PageResponse<AuditEventItem> search(LoginAccount actor, String actorId, String action, String from, String to,
                                               String sort, int page, int size) {
        PageParams.validate(page, size);
        AuditSort auditSort = AuditSort.parse(sort);
        if ((actorId != null && actorId.length() > 36) || (action != null && action.length() > 40)) {
            throw ApiException.badRequest();
        }
        AuditSearch search = new AuditSearch(blankToNull(actorId), blankToNull(action), from == null || from.isBlank() ? null : parseDate(from).toString(), blankToNull(to),
                auditSort.field(), auditSort.descending(), page * size, size);
        List<AuditEventItem> items = auditMapper.searchEvents(search).stream()
                .map(event -> new AuditEventItem(event.getEventId(), event.getActorId(), event.getAction(),
                        event.getTargetType(), event.getTargetId(), event.getCustomerId(), event.getResult(),
                        event.getOccurredAt(), maskPhones(event.getReason())))
                .toList();
        auditRecorder.success(actor, "AUDIT_LIST_VIEW", "AUDIT", null, null, null);
        return new PageResponse<>(items, page, auditMapper.countEvents(search));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest();
        }
    }

    private String maskPhones(String reason) {
        return reason == null ? null : reason.replaceAll("[0-9]{3}-[0-9]{4}-[0-9]{4}", "***-****-****");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
