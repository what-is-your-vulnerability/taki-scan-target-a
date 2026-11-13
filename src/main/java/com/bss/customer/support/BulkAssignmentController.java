package com.bss.customer.support;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bss.customer.security.LoginAccount;

@RestController
@RequestMapping("/api/v1/admin/customers")
public class BulkAssignmentController {

    private final BulkAssignmentService service;

    public BulkAssignmentController(BulkAssignmentService service) {
        this.service = service;
    }

    @PostMapping(value = "/bulk-assignments", consumes = "application/xml")
    @PreAuthorize("hasRole('ADMIN')")
    public BulkResponse assign(@AuthenticationPrincipal LoginAccount actor, @RequestBody String xml) {
        return new BulkResponse(service.assign(actor, xml));
    }

    public record BulkResponse(List<BulkAssignmentService.Result> results) {
    }
}
