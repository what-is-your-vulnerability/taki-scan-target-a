package com.bss.customer.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bss.customer.security.LoginAccount;

@Controller
public class MemberViewController {

    private final MemberAdminService memberAdminService;

    public MemberViewController(MemberAdminService memberAdminService) {
        this.memberAdminService = memberAdminService;
    }

    @GetMapping("/api/v1/admin/members/{account_id}/view")
    public String view(@AuthenticationPrincipal LoginAccount actor,
                       @PathVariable("account_id") String accountId,
                       Model model) {
        model.addAttribute("member", memberAdminService.detail(actor, accountId));
        return "admin/member-detail";
    }
}
