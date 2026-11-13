package com.bss.customer.support;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bss.customer.security.LoginAccount;

@RestController
@RequestMapping("/api/v1/admin/members")
public class MemberLookupController {

    private final MemberLookupService service;

    public MemberLookupController(MemberLookupService service) {
        this.service = service;
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasRole('ADMIN')")
    public LookupResponse lookup(@AuthenticationPrincipal LoginAccount actor,
                                 @RequestParam("login_id_prefix") String loginIdPrefix,
                                 @RequestParam(defaultValue = "login_id") String sort) {
        return new LookupResponse(service.lookup(actor, loginIdPrefix, sort));
    }

    public record LookupResponse(List<MemberLookupItem> items) {
    }
}
